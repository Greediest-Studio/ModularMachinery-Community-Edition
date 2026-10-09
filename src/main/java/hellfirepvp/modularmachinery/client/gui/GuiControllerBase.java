package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.client.gui.widget.GuiControllerModeSelector;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public abstract class GuiControllerBase<T extends ContainerBase<? extends TileMultiblockMachineController>> extends GuiContainerBase<T> {
    protected final GuiControllerModeSelector modeSelector;
    protected boolean modeScrollHandled;
    protected boolean modeClickHandled;

    protected GuiControllerBase(T container) {
        super(container);
        modeSelector = new GuiControllerModeSelector(container);
    }

    @Override
    public void initGui() {
        super.initGui();
        modeSelector.close();
        modeClickHandled = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        modeSelector.draw(guiLeft, guiTop, xSize, width, height, mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (!modeSelector.isOpen() && !modeSelector.isMouseOverButtons(mouseX, mouseY)) {
            super.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!handleModeMouseClick(mouseX, mouseY, mouseButton)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected boolean handleModeMouseClick(int mouseX, int mouseY, int mouseButton) {
        modeClickHandled = modeSelector.mouseClicked(mouseX, mouseY, mouseButton);
        return modeClickHandled;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // 选择列表可能覆盖物品槽，按下和松开必须一起拦截。
        if (modeClickHandled || modeSelector.isOpen()) {
            modeClickHandled = false;
            return;
        }
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long elapsed) {
        if (!modeClickHandled && !modeSelector.isOpen()) {
            super.mouseClickMove(mouseX, mouseY, mouseButton, elapsed);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        modeScrollHandled = modeSelector.mouseScrolled(mouseX, mouseY, Mouse.getEventDWheel());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!modeSelector.keyTyped(keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
