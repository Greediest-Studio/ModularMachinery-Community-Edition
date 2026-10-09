package hellfirepvp.modularmachinery.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiScrollableTextPanel {
    public static final double FONT_SCALE = 0.72;

    // 内边距、滚动条和滚动位置使用 GUI 像素；行高和段距使用缩放前的字体像素。
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 12;
    private static final int PARAGRAPH_SPACING = 6;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_INSET = 2;
    private static final int MIN_THUMB_HEIGHT = 12;
    private static final int SCROLL_STEP = (int) Math.ceil(LINE_HEIGHT * FONT_SCALE * 3);

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<TextLine> lines = new ArrayList<>();

    private FontRenderer font;
    private int nextLineY;
    private int textHeight;
    private int scrollOffset;
    private int maxScroll;
    private boolean dragging;
    private int dragOffset;

    public GuiScrollableTextPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void begin(FontRenderer font) {
        this.font = font;
        lines.clear();
        nextLineY = 0;
        textHeight = 0;
    }

    public void addLine(String text) {
        // 给文字阴影留一个字体像素，右侧内边距容纳细滚动条。
        int wrapWidth = MathHelper.floor((width - PADDING * 2) / FONT_SCALE) - 1;
        for (String line : font.listFormattedStringToWidth(text, wrapWidth)) {
            lines.add(new TextLine(line, nextLineY));
            textHeight = nextLineY + font.FONT_HEIGHT + 1;
            nextLineY += LINE_HEIGHT;
        }
    }

    public void addParagraphSpacing() {
        nextLineY += PARAGRAPH_SPACING;
    }

    public void draw(Minecraft mc, int guiLeft, int guiTop, int mouseX, int mouseY) {
        int viewportHeight = getViewportHeight();
        maxScroll = Math.max(0, (int) Math.ceil(textHeight * FONT_SCALE) - viewportHeight);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);

        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // 裁剪坐标来自窗口像素，不受下面的文字缩放影响。
        GL11.glScissor((guiLeft + x + PADDING) * scaleFactor,
            mc.displayHeight - (guiTop + y + height - PADDING) * scaleFactor,
            (width - PADDING * 2) * scaleFactor, viewportHeight * scaleFactor);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x + PADDING, y + PADDING - scrollOffset, 0);
            GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            for (TextLine line : lines) {
                // 边缘只显示完整行，避免中文被截成零碎笔画。
                if (line.y * FONT_SCALE >= scrollOffset
                    && (line.y + font.FONT_HEIGHT + 1) * FONT_SCALE <= scrollOffset + viewportHeight) {
                    font.drawStringWithShadow(line.text, 0, line.y, 0xFFFFFF);
                }
            }
        } finally {
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (maxScroll > 0) {
            int left = x + width - SCROLLBAR_INSET - SCROLLBAR_WIDTH;
            int top = y + PADDING;
            int thumbTop = top + getThumbOffset();
            Gui.drawRect(left, top, left + SCROLLBAR_WIDTH, top + viewportHeight, 0xFF303030);
            Gui.drawRect(left, thumbTop, left + SCROLLBAR_WIDTH, thumbTop + getThumbHeight(),
                dragging || isMouseOverScrollbar(mouseX, mouseY) ? 0xFFD0D0D0 : 0xFF909090);
        }
    }

    public boolean mouseScrolled(int mouseX, int mouseY, int wheel) {
        if (wheel == 0 || mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return false;
        }
        scrollOffset = MathHelper.clamp(scrollOffset + (wheel > 0 ? -SCROLL_STEP : SCROLL_STEP), 0, maxScroll);
        // 即使文字未溢出，也消费面板内的滚轮，避免带动旁边的配方列表。
        return true;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || maxScroll == 0 || !isMouseOverScrollbar(mouseX, mouseY)) {
            return false;
        }
        int thumbTop = y + PADDING + getThumbOffset();
        int thumbHeight = getThumbHeight();
        dragOffset = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight
            ? mouseY - thumbTop : thumbHeight / 2;
        dragging = true;
        dragTo(mouseY);
        return true;
    }

    public boolean mouseDragged(int mouseY, int mouseButton) {
        if (!dragging || mouseButton != 0) {
            return false;
        }
        if (maxScroll > 0) {
            dragTo(mouseY);
        }
        return true;
    }

    public boolean mouseReleased(int mouseButton) {
        if (!dragging || mouseButton != 0) {
            return false;
        }
        dragging = false;
        return true;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void resetScroll() {
        scrollOffset = 0;
        dragging = false;
    }

    private void dragTo(int mouseY) {
        int travel = getViewportHeight() - getThumbHeight();
        scrollOffset = MathHelper.clamp(
            Math.round((mouseY - y - PADDING - dragOffset) * (float) maxScroll / travel), 0, maxScroll);
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        return mouseX >= x + width - PADDING && mouseX < x + width
            && mouseY >= y + PADDING && mouseY < y + height - PADDING;
    }

    private int getViewportHeight() {
        return height - PADDING * 2;
    }

    private int getThumbHeight() {
        int viewportHeight = getViewportHeight();
        return Math.max(MIN_THUMB_HEIGHT, viewportHeight * viewportHeight / (viewportHeight + maxScroll));
    }

    private int getThumbOffset() {
        return Math.round(scrollOffset * (float) (getViewportHeight() - getThumbHeight()) / maxScroll);
    }

    private static class TextLine {
        private final String text;
        private final int y;

        private TextLine(String text, int y) {
            this.text = text;
            this.y = y;
        }
    }
}
