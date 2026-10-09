package hellfirepvp.modularmachinery.client.gui.widget;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.network.PktControllerModeSelect;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ControllerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 普通控制器与工厂控制器共用的齿轮按钮和模式列表。坐标均为屏幕坐标。 */
public class GuiControllerModeSelector {
    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_STEP = 20;
    private static final int MAX_BUTTONS = 5;
    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 20;
    private static final int MAX_ROWS = 6;

    private final ContainerBase<? extends TileMultiblockMachineController> container;
    private final GuiButton button = new GuiButton(0, 0, 0, BUTTON_SIZE, BUTTON_SIZE, "");
    private final List<ControllerMode> types = new ArrayList<>();
    private ControllerMode openedType;
    private int buttonX;
    private int buttonBottomY;
    private int buttonScroll;
    private int modeScroll;
    private int popupX;
    private int popupY;
    private int popupWidth;
    private int popupHeight;
    private int visibleRows;

    public GuiControllerModeSelector(ContainerBase<? extends TileMultiblockMachineController> container) {
        this.container = container;
    }

    public void draw(int guiLeft, int guiTop, int guiWidth, int screenWidth, int screenHeight,
                     int mouseX, int mouseY) {
        buttonX = guiLeft + guiWidth - 26;
        buttonBottomY = guiTop + 106;
        refreshTypes();
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);
        for (int i = buttonScroll; i < Math.min(types.size(), buttonScroll + MAX_BUTTONS); i++) {
            ControllerMode type = types.get(i);
            button.x = buttonX;
            button.y = buttonY(i);
            button.enabled = canSelect(type);
            button.drawButton(mc, mouseX, mouseY, 0);
            drawGear(button.x + 9, button.y + 9, button.enabled ? 0xFFE0E3E6 : 0xFF777777);
            // 一个可见按钮时不编号；多个按钮按脚本注册顺序显示角标。
            if (types.size() > 1) {
                String number = Integer.toString(i + 1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(button.x + 17 - font.getStringWidth(number) * 0.75F, button.y + 11, 1);
                GlStateManager.scale(0.75F, 0.75F, 1);
                font.drawStringWithShadow(number, 0, 0, button.enabled ? 0xFFFFFF : 0xAAAAAA);
                GlStateManager.popMatrix();
            }
        }
        if (isOpen()) {
            drawPopup(font, screenWidth, screenHeight, mouseX, mouseY);
        }
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();

        if (!isOpen()) {
            int hovered = hoveredButton(mouseX, mouseY);
            if (hovered >= 0) {
                GuiUtils.drawHoveringText(tooltip(types.get(hovered)), mouseX, mouseY,
                    screenWidth, screenHeight, -1, font);
            }
        }
    }

    private void refreshTypes() {
        DynamicMachine machine = container.getOwner().getControllerModeMachine();
        types.clear();
        if (machine != null) {
            for (ControllerMode type : machine.getControllerModes().values()) {
                if (type.isControllerButtonVisible()) {
                    types.add(type);
                }
            }
        }
        buttonScroll = MathHelper.clamp(buttonScroll, 0, Math.max(0, types.size() - MAX_BUTTONS));
        if (openedType != null && (!types.contains(openedType) || !canSelect(openedType))) {
            close();
        }
    }

    private boolean canSelect(ControllerMode type) {
        return container.getOwner().isStructureFormed() && !type.getModes().isEmpty();
    }

    private List<String> tooltip(ControllerMode type) {
        String modeName;
        if (!container.getOwner().isStructureFormed()) {
            modeName = I18n.format("gui.controller.mode.unavailable");
        } else {
            int value = container.getOwner().getControllerMode(type.getName());
            modeName = type.getModes().get(value);
            if (modeName == null) {
                modeName = I18n.format("gui.controller.mode.unknown", Integer.toString(value));
            }
        }
        List<String> tooltip = new ArrayList<>();
        String instruction = type.getControllerButtonTooltip();
        tooltip.add(I18n.hasKey(instruction) ? I18n.format(instruction) : instruction);
        String currentMode = type.getControllerButtonCurrentModeTooltip();
        tooltip.add(I18n.hasKey(currentMode) ? I18n.format(currentMode, modeName) : currentMode.replace("%s", modeName));
        if (!container.getOwner().isStructureFormed()) {
            tooltip.add("\u00a7c" + I18n.format("gui.controller.status.missing_structure"));
        } else if (type.getModes().isEmpty()) {
            tooltip.add("\u00a7c" + I18n.format("gui.controller.mode.empty"));
        }
        return tooltip;
    }

    private void drawPopup(FontRenderer font, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        List<Map.Entry<Integer, String>> modes = new ArrayList<>(openedType.getModes().entrySet());
        String title = I18n.format("gui.controller.mode.title", openedType.getName());
        popupWidth = Math.max(112, font.getStringWidth(title) + 12);
        for (Map.Entry<Integer, String> mode : modes) {
            popupWidth = Math.max(popupWidth, font.getStringWidth(mode.getValue()) + 26);
        }
        popupWidth = Math.min(popupWidth, Math.min(180, screenWidth - 8));
        visibleRows = Math.min(modes.size(), Math.max(1, Math.min(MAX_ROWS, (screenHeight - 44) / ROW_HEIGHT)));
        modeScroll = MathHelper.clamp(modeScroll, 0, modes.size() - visibleRows);
        boolean scrolling = modes.size() > visibleRows;
        popupHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + (scrolling ? 15 : 4);
        popupX = Math.max(4, buttonX - popupWidth - 4);
        popupY = MathHelper.clamp(buttonY(types.indexOf(openedType)) + BUTTON_SIZE - popupHeight,
            4, Math.max(4, screenHeight - popupHeight - 4));

        Gui.drawRect(popupX - 1, popupY - 1, popupX + popupWidth + 1, popupY + popupHeight + 1, 0xFF92979F);
        Gui.drawRect(popupX, popupY, popupX + popupWidth, popupY + popupHeight, 0xFF171A20);
        font.drawStringWithShadow(font.trimStringToWidth(title, popupWidth - 10), popupX + 5, popupY + 6, 0xFFFFFF);
        int selectedValue = container.getOwner().getControllerMode(openedType.getName());
        for (int row = 0; row < visibleRows; row++) {
            Map.Entry<Integer, String> mode = modes.get(modeScroll + row);
            int y = popupY + HEADER_HEIGHT + row * ROW_HEIGHT;
            boolean selected = selectedValue == mode.getKey();
            boolean hovered = contains(mouseX, mouseY, popupX + 3, y, popupWidth - 6, ROW_HEIGHT - 1);
            int color = hovered ? 0xFF454D5A : selected ? 0xFF304C3E : 0xFF252A32;
            Gui.drawRect(popupX + 3, y, popupX + popupWidth - 3, y + ROW_HEIGHT - 1, color);
            if (selected) {
                font.drawStringWithShadow(">", popupX + 6, y + 5, 0x80FFAA);
            }
            font.drawStringWithShadow(font.trimStringToWidth(mode.getValue(), popupWidth - 24),
                popupX + 16, y + 5, selected ? 0xA5FFC0 : 0xFFFFFF);
        }
        if (scrolling) {
            String page = (modeScroll + 1) + "-" + (modeScroll + visibleRows) + " / " + modes.size();
            font.drawStringWithShadow(page, popupX + popupWidth - font.getStringWidth(page) - 5,
                popupY + popupHeight - 12, 0xAAAAAA);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        refreshTypes();
        int hovered = hoveredButton(mouseX, mouseY);
        if (hovered >= 0) {
            if (mouseButton == 0 && canSelect(types.get(hovered))) {
                ControllerMode type = types.get(hovered);
                openedType = type == openedType ? null : type;
                modeScroll = 0;
                button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            }
            return true;
        }
        if (!isOpen()) {
            return false;
        }
        if (!contains(mouseX, mouseY, popupX, popupY, popupWidth, popupHeight)) {
            close();
            return true;
        }
        if (mouseButton == 0 && contains(mouseX, mouseY, popupX + 3, popupY + HEADER_HEIGHT,
            popupWidth - 6, visibleRows * ROW_HEIGHT)) {
            int index = modeScroll + (mouseY - popupY - HEADER_HEIGHT) / ROW_HEIGHT;
            List<Integer> values = new ArrayList<>(openedType.getModes().keySet());
            ModularMachinery.NET_CHANNEL.sendToServer(new PktControllerModeSelect(
                container.windowId, openedType.getName(), values.get(index)));
            button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            close();
        }
        return true;
    }

    public boolean mouseScrolled(int mouseX, int mouseY, int wheel) {
        if (wheel == 0) {
            return false;
        }
        int step = wheel > 0 ? -1 : 1;
        if (isOpen()) {
            modeScroll = MathHelper.clamp(modeScroll + step, 0, Math.max(0, openedType.getModes().size() - visibleRows));
            return true;
        }
        if (isMouseOverButtons(mouseX, mouseY)) {
            buttonScroll = MathHelper.clamp(buttonScroll + step, 0, Math.max(0, types.size() - MAX_BUTTONS));
            return true;
        }
        return false;
    }

    public boolean keyTyped(int keyCode) {
        if (!isOpen()) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
        }
        return true;
    }

    public boolean isOpen() {
        return openedType != null;
    }

    public void close() {
        openedType = null;
        modeScroll = 0;
    }

    public boolean isMouseOverButtons(int mouseX, int mouseY) {
        int count = Math.min(MAX_BUTTONS, types.size());
        return count > 0 && contains(mouseX, mouseY, buttonX, buttonBottomY - (count - 1) * BUTTON_STEP,
            BUTTON_SIZE, (count - 1) * BUTTON_STEP + BUTTON_SIZE);
    }

    private int hoveredButton(int mouseX, int mouseY) {
        for (int i = buttonScroll; i < Math.min(types.size(), buttonScroll + MAX_BUTTONS); i++) {
            if (contains(mouseX, mouseY, buttonX, buttonY(i), BUTTON_SIZE, BUTTON_SIZE)) {
                return i;
            }
        }
        return -1;
    }

    private int buttonY(int index) {
        int visibleButtons = Math.min(MAX_BUTTONS, types.size() - buttonScroll);
        return buttonBottomY - (visibleButtons - 1 - (index - buttonScroll)) * BUTTON_STEP;
    }

    private static boolean contains(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** 直接画齿轮轮廓，避免依赖字体是否提供齿轮字符或额外模组贴图。 */
    private static void drawGear(int x, int y, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * (i - 0.5) / 16;
            double nextAngle = Math.PI * (i + 0.5) / 16;
            double radius = i % 4 < 2 ? 6 : 4.5;
            double nextRadius = (i + 1) % 4 < 2 ? 6 : 4.5;
            gearVertex(buffer, x, y, angle, 2.2, color);
            gearVertex(buffer, x, y, nextAngle, 2.2, color);
            gearVertex(buffer, x, y, nextAngle, nextRadius, color);
            gearVertex(buffer, x, y, angle, radius, color);
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
    }

    private static void gearVertex(BufferBuilder buffer, int x, int y, double angle, double radius, int color) {
        buffer.pos(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius, 0)
            .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >>> 24) & 255).endVertex();
    }
}
