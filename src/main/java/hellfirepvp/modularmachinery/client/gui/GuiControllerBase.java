package hellfirepvp.modularmachinery.client.gui;

import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.client.gui.widget.GuiControllerModeSelector;
import hellfirepvp.modularmachinery.client.gui.widget.GuiScrollableTextPanel;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public abstract class GuiControllerBase<T extends ContainerBase<? extends TileMultiblockMachineController>> extends GuiContainerBase<T> {
    protected final GuiControllerModeSelector modeSelector;
    protected final GuiScrollableTextPanel statusPanel;
    protected boolean controllerScrollHandled;
    protected boolean modeClickHandled;

    protected GuiControllerBase(T container, int statusPanelX) {
        super(container);
        modeSelector = new GuiControllerModeSelector(container);
        statusPanel = new GuiScrollableTextPanel(statusPanelX, 7, 136, 116);
    }

    @Override
    public void initGui() {
        super.initGui();
        modeSelector.close();
        statusPanel.resetScroll();
        modeClickHandled = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        modeSelector.draw(guiLeft, guiTop, xSize, width, height, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        statusPanel.begin(fontRenderer);
        populateStatusPanel();
        statusPanel.draw(mc, guiLeft, guiTop, mouseX - guiLeft, mouseY - guiTop);
    }

    private void populateStatusPanel() {
        TileMultiblockMachineController controller = container.getOwner();
        if (controller.getWorld().getStrongPower(controller.getPos()) > 0) {
            statusPanel.addLine(I18n.format("gui.controller.status.redstone_stopped"));
            return;
        }

        DynamicMachine blueprint = controller.getBlueprintMachine();
        if (blueprint != null) {
            statusPanel.addLine(I18n.format("gui.controller.blueprint", ""));
            statusPanel.addLine(blueprint.getLocalizedName());
            statusPanel.addParagraphSpacing();
        } else if (!controller.isStructureFormed()) {
            statusPanel.addLine(I18n.format("gui.controller.blueprint", I18n.format("gui.controller.blueprint.none")));
            statusPanel.addParagraphSpacing();
        }

        DynamicMachine found = controller.getFoundMachine();
        if (found != null) {
            statusPanel.addLine(I18n.format("gui.controller.structure", ""));
            statusPanel.addLine(found.getLocalizedName());

            ControllerGUIRenderEvent event = new ControllerGUIRenderEvent(controller);
            event.postEvent();
            String[] extraInfo = event.getExtraInfo();
            if (extraInfo.length != 0) {
                statusPanel.addParagraphSpacing();
                for (String line : extraInfo) {
                    statusPanel.addLine(line);
                }
            }
        } else {
            statusPanel.addLine(I18n.format("gui.controller.structure", I18n.format("gui.controller.structure.none")));
        }
        statusPanel.addParagraphSpacing();
        addControllerStatus();
    }

    protected abstract void addControllerStatus();

    protected boolean drawCustomBackground(boolean factoryController) {
        // 未成型时仍可从专属控制器或蓝图取得背景，每帧取值以响应蓝图切换。
        DynamicMachine machine = container.getOwner().getControllerModeMachine();
        if (machine == null) {
            return false;
        }
        ResourceLocation background = factoryController
            ? machine.getFactoryGuiBackground() : machine.getControllerGuiBackground();
        if (background == null) {
            return false;
        }

        TextureManager textures = mc.getTextureManager();
        textures.bindTexture(background);
        // 复用纹理管理器的失败缓存；F3+T 会清除此缓存并允许重新加载。
        if (textures.getTexture(background) == TextureUtil.MISSING_TEXTURE) {
            return false;
        }
        // 自定义图片使用完整 UV，按面板尺寸缩放，不沿用原图的图集裁切。
        Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, xSize, ySize);
        return true;
    }

    protected void addPerformanceInfo() {
        statusPanel.addLine(String.format("Avg: %sμs/t (Search: %sms), WorkMode: %s",
            TileMultiblockMachineController.usedTimeCache,
            MiscUtils.formatFloat(TileMultiblockMachineController.searchUsedTimeCache / 1000F, 2),
            TileMultiblockMachineController.workModeCache.getDisplayName()));
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (!modeSelector.isOpen() && !modeSelector.isMouseOverButtons(mouseX, mouseY)) {
            super.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!handleModeMouseClick(mouseX, mouseY, mouseButton)
            && !statusPanel.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected boolean handleModeMouseClick(int mouseX, int mouseY, int mouseButton) {
        modeClickHandled = modeSelector.mouseClicked(mouseX, mouseY, mouseButton);
        return modeClickHandled;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (statusPanel.mouseReleased(mouseButton)) {
            return;
        }
        // 选择列表可能覆盖物品槽，按下和松开必须一起拦截。
        if (modeClickHandled || modeSelector.isOpen()) {
            modeClickHandled = false;
            return;
        }
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long elapsed) {
        if (!modeClickHandled && !modeSelector.isOpen()
            && !statusPanel.mouseDragged(mouseY - guiTop, mouseButton)) {
            super.mouseClickMove(mouseX, mouseY, mouseButton, elapsed);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int wheel = Mouse.getEventDWheel();
        controllerScrollHandled = modeSelector.mouseScrolled(mouseX, mouseY, wheel)
            || statusPanel.mouseScrolled(mouseX - guiLeft, mouseY - guiTop, wheel);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!modeSelector.keyTyped(keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
