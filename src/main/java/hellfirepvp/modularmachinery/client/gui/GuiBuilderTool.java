package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerBuilderTool;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import hellfirepvp.modularmachinery.common.network.PktBuilderConfig;
import hellfirepvp.modularmachinery.common.network.PktBuilderVariable;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.*;
import org.lwjgl.input.Keyboard;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public final class GuiBuilderTool extends GuiContainerBase<ContainerBuilderTool> {
    private int page;
    private int offset;
    private String alias;
    private GuiTextField length;
    private GuiTextField attachment;
    private final String[] keys = {"disassemble_mode", "use_ae_items", "use_ae_fluids", "craft_missing", "variables.skip_existing"};
    public GuiBuilderTool(ContainerBuilderTool container) { super(container); }
    @Override protected void setWidthHeight() { xSize = 320; ySize = 240; }
    @Override public void initGui() { super.initGui(); Keyboard.enableRepeatEvents(true); refresh(); }

    private String text(String key) { return I18n.format("gui.modularmachinery.builder." + key); }
    private GuiButton button(int id, int x, int y, int w, String title) {
        GuiButton button = new GuiButton(id, guiLeft + x, guiTop + y, w, 18, title);
        button.enabled = container.ready;
        buttonList.add(button);
        return button;
    }
    public void refresh() {
        if (fontRenderer == null) return;
        buttonList.clear();
        length = null; attachment = null;
        if (page == 0) {
            button(100, 240, 7, 70, text("optional_blocks.open"));
            boolean[] values = {container.options.disassemble, container.options.useAeItems, container.options.useAeFluids, container.options.craftMissing, container.options.skipExisting};
            for (int i = 0; i < values.length; i++) {
                GuiButton b = button(i, 242, 29 + i * 18, 68, text(values[i] ? "on" : "off"));
                if (i >= 1 && i <= 3) b.enabled &= container.hasAe;
            }
            length = field(20, 242, 121, 68, String.valueOf(container.options.dynamicLength), 4);
            if (container.hasComplement) attachment = field(21, 105, 140, 205, container.options.attachmentModule, 256);
        } else {
            button(101, 10, 7, 58, text("back"));
            button(102, 10, 132, 40, "<");
            button(103, 270, 132, 40, ">");
            if (page == 1) {
                List<String> names = new ArrayList<>(container.catalog.keySet());
                offset = Math.max(0, Math.min(offset, Math.max(0, (names.size() - 1) / 5)));
                for (int i = 0; i < 5 && offset * 5 + i < names.size(); i++) {
                    String name = names.get(offset * 5 + i);
                    String label = name + (container.options.variables.containsKey(name) ? " *" : "");
                    button(200 + i, 15, 40 + i * 18, 225, fontRenderer.trimStringToWidth(label, 211));
                }
                button(104, 247, 89, 63, text("variables.clear"));
            } else {
                button(105, 90, 132, 140, text("variables.pattern_default"));
                offset = Math.max(0, Math.min(offset, Math.max(0, (candidates().size() - 1) / 56)));
            }
        }
    }
    private GuiTextField field(int id, int x, int y, int width, String value, int max) {
        GuiTextField field = new GuiTextField(id, fontRenderer, guiLeft + x, guiTop + y, width, 15);
        field.setMaxStringLength(max); field.setText(value); field.setEnabled(container.ready);
        return field;
    }
    private List<String> candidates() { return container.catalog.getOrDefault(alias, Collections.emptyList()); }
    @Override protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0 -> container.options.disassemble = !container.options.disassemble;
            case 1 -> container.options.useAeItems = !container.options.useAeItems;
            case 2 -> container.options.useAeFluids = !container.options.useAeFluids;
            case 3 -> container.options.craftMissing = !container.options.craftMissing;
            case 4 -> container.options.skipExisting = !container.options.skipExisting;
            case 100 -> { commitFields(); page = 1; offset = 0; }
            case 101 -> { page--; offset = 0; }
            case 102 -> offset = Math.max(0, offset - 1);
            case 103 -> offset++;
            case 104 -> select(BuilderVariables.ALL, "");
            case 105 -> select(alias, "");
            default -> {
                if (button.id >= 200) {
                    alias = new ArrayList<>(container.catalog.keySet()).get(offset * 5 + button.id - 200);
                    page = 2; offset = 0;
                }
            }
        }
        if (button.id <= 4) sendSettings();
        refresh();
    }
    private void sendSettings() {
        if (container.ready) ModularMachinery.NET_CHANNEL.sendToServer(new PktBuilderConfig(container.windowId, BuilderToolSettings.write(container.options)));
    }
    private void commitFields() {
        if (length == null || !container.ready) return;
        try { container.options.dynamicLength = Math.max(0, Math.min(4096, Integer.parseInt(length.getText()))); }
        catch (NumberFormatException ignored) { /* 编辑中允许暂时留空，保留上次有效数值。 */ }
        if (attachment != null) container.options.attachmentModule = attachment.getText().trim();
        sendSettings();
    }
    private void select(String name, String spec) {
        if (!container.ready) return;
        if (spec.isEmpty()) container.options.variables.remove(name);
        else container.options.variables.put(name, spec);
        ModularMachinery.NET_CHANNEL.sendToServer(new PktBuilderVariable(container.windowId, name, spec));
    }
    public Rectangle ghostArea() { return new Rectangle(guiLeft + 269, guiTop + 57, 20, 20); }
    public boolean showsGhostSlot() { return page == 1 && container.ready; }
    public void acceptGhost(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock block)) return;
        select(BuilderVariables.ALL, block.getBlock().getRegistryName() + "@" + block.getMetadata(stack.getMetadata()));
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showsGhostSlot() && ghostArea().contains(mouseX, mouseY)) {
            if (mouseButton == 1) select(BuilderVariables.ALL, "");
            else {
                ItemStack stack = mc.player.inventory.getItemStack();
                if (!(stack.getItem() instanceof ItemBlock)) stack = mc.player.getHeldItemOffhand();
                acceptGhost(stack);
            }
            return;
        }
        if (page == 2 && mouseButton == 0) {
            int x = mouseX - guiLeft - 34, y = mouseY - guiTop - 45;
            if (x >= 0 && x < 252 && y >= 0 && y < 72) {
                int index = offset * 56 + (y / 18) * 14 + x / 18;
                if (index < candidates().size()) select(alias, candidates().get(index));
                return;
            }
        }
        if (length != null) {
            boolean wasFocused = length.isFocused() || attachment != null && attachment.isFocused();
            length.mouseClicked(mouseX, mouseY, mouseButton);
            if (attachment != null) attachment.mouseClicked(mouseX, mouseY, mouseButton);
            if (wasFocused && !length.isFocused() && (attachment == null || !attachment.isFocused())) commitFields();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    @Override protected void keyTyped(char character, int key) throws IOException {
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) { commitFields(); return; }
        if (length != null && length.textboxKeyTyped(character, key)) { commitFields(); return; }
        if (attachment != null && attachment.textboxKeyTyped(character, key)) { commitFields(); return; }
        super.keyTyped(character, key);
    }
    @Override public void onGuiClosed() { commitFields(); Keyboard.enableRepeatEvents(false); super.onGuiClosed(); }
    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFB9B9B9);
        drawRect(guiLeft + 3, guiTop + 3, guiLeft + xSize - 3, guiTop + 153, 0xFF333941);
        for (net.minecraft.inventory.Slot slot : container.inventorySlots) {
            int x = guiLeft + slot.xPos, y = guiTop + slot.yPos;
            drawRect(x - 1, y - 1, x + 17, y + 17, 0xFFDDDDDD);
            drawRect(x - 1, y - 1, x + 16, y + 16, 0xFF555555);
            drawRect(x, y, x + 16, y + 16, 0xFF888888);
        }
        fontRenderer.drawString(text("title"), guiLeft + (page == 0 ? 12 : 78), guiTop + 12, 0xFFFFFF);
        if (page == 0) {
            for (int i = 0; i < keys.length; i++) fontRenderer.drawString(text(keys[i]), guiLeft + 12, guiTop + 34 + i * 18, 0xFFFFFF);
            fontRenderer.drawString(text("dynamic_length"), guiLeft + 12, guiTop + 124, 0xFFFFFF);
            if (length != null) length.drawTextBox();
            if (attachment != null) {
                fontRenderer.drawString(text("attachment_module"), guiLeft + 12, guiTop + 141, 0xFFFFFF);
                attachment.drawTextBox();
            }
        } else if (page == 1) {
            fontRenderer.drawString(text("variables.all"), guiLeft + 248, guiTop + 42, 0xFFFFFF);
            Rectangle r = ghostArea(); drawRect(r.x, r.y, r.x + r.width, r.y + r.height, 0xFF888888);
            drawItem(container.options.variables.get(BuilderVariables.ALL), r.x + 2, r.y + 2);
            if (container.catalog.isEmpty()) fontRenderer.drawSplitString(text("variables.none"), guiLeft + 15, guiTop + 45, 215, 0xFFFFFF);
        } else {
            fontRenderer.drawString(fontRenderer.trimStringToWidth(alias, 285), guiLeft + 16, guiTop + 31, 0xFFFFFF);
            List<String> specs = candidates();
            for (int i = 0; i < 56 && offset * 56 + i < specs.size(); i++) {
                int x = guiLeft + 34 + i % 14 * 18, y = guiTop + 45 + i / 14 * 18;
                String spec = specs.get(offset * 56 + i);
                drawRect(x, y, x + 18, y + 18, spec.equals(container.options.variables.get(alias)) ? 0xFF709958 : 0xFF777777);
                drawItem(spec, x + 1, y + 1);
            }
        }
        if (page > 0) fontRenderer.drawString(String.valueOf(offset + 1), guiLeft + 61, guiTop + 137, 0xFFFFFF);
    }
    private void drawItem(String spec, int x, int y) {
        ItemStack stack = stack(spec);
        if (!stack.isEmpty()) { net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting(); itemRender.renderItemAndEffectIntoGUI(stack, x, y); net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting(); }
    }
    private ItemStack stack(String spec) {
        if (spec == null) return ItemStack.EMPTY;
        int at = spec.lastIndexOf('@');
        try { return new ItemStack(Block.REGISTRY.getObject(new ResourceLocation(at < 0 ? spec : spec.substring(0, at))), 1, at < 0 ? 0 : Integer.parseInt(spec.substring(at + 1))); }
        catch (RuntimeException ignored) { return ItemStack.EMPTY; }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (page == 2) {
            int x = mouseX - guiLeft - 34, y = mouseY - guiTop - 45;
            int i = offset * 56 + y / 18 * 14 + x / 18;
            if (x >= 0 && x < 252 && y >= 0 && y < 72 && i < candidates().size()) renderToolTip(stack(candidates().get(i)), mouseX, mouseY);
        }
    }
}
