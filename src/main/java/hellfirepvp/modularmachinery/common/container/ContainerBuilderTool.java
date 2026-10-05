package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.item.ItemAdvancedBuilderTool;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import hellfirepvp.modularmachinery.common.network.PktBuilderData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ContainerBuilderTool extends Container {
    private final EntityPlayer owner;
    private final int toolSlot;
    private final ItemStack tool;
    public AssemblyOptions options;
    public Map<String, List<String>> catalog = new TreeMap<>();
    public boolean hasAe;
    public boolean hasFluidCrafting;
    public boolean hasComplement;
    public boolean ready;

    public ContainerBuilderTool(EntityPlayer player, int slot) {
        owner = player;
        toolSlot = slot;
        tool = slot >= 0 && slot < player.inventory.getSizeInventory() ? player.inventory.getStackInSlot(slot) : ItemStack.EMPTY;
        options = BuilderToolSettings.read(tool);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 69 + col * 18, 156 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, col, 69 + col * 18, 214));
        addSlotToContainer(new Slot(player.inventory, 40, 249, 214));
        if (!player.world.isRemote) {
            catalog = BuilderVariables.catalog();
            hasAe = Mods.AE2.isPresent();
            hasFluidCrafting = net.minecraftforge.fml.common.Loader.isModLoaded("ae2fc");
            hasComplement = hellfirepvp.modularmachinery.common.integration.mmcecomplement.AttachmentModuleCompat.isAvailable();
            ready = true;
        }
    }
    @Override public boolean canInteractWith(@NotNull EntityPlayer player) {
        return player == owner && !tool.isEmpty() && tool.getItem() instanceof ItemAdvancedBuilderTool
            && player.inventory.getStackInSlot(toolSlot) == tool;
    }
    @Override public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) { return ItemStack.EMPTY; }
    @Override public @NotNull ItemStack slotClick(int slot, int button, @NotNull ClickType type, @NotNull EntityPlayer player) {
        if (slot >= 0 && slot < inventorySlots.size() && inventorySlots.get(slot).getSlotIndex() == toolSlot) return ItemStack.EMPTY;
        if (type == ClickType.SWAP && button == toolSlot) return ItemStack.EMPTY;
        return super.slotClick(slot, button, type, player);
    }
    @Override public void addListener(@NotNull IContainerListener listener) {
        super.addListener(listener);
        if (listener instanceof EntityPlayerMP player) sync(player);
    }
    public void sync(EntityPlayerMP player) { ModularMachinery.NET_CHANNEL.sendTo(new PktBuilderData(this), player); }
    public void applySettings(NBTTagCompound tag) {
        AssemblyOptions edited = BuilderToolSettings.read(tag);
        options.disassemble = edited.disassemble;
        options.useAeItems = hasAe && edited.useAeItems;
        options.useAeFluids = hasAe && edited.useAeFluids;
        options.craftMissing = hasAe && edited.craftMissing;
        options.skipExisting = edited.skipExisting;
        options.dynamicLength = edited.dynamicLength;
        options.attachmentModule = hasComplement ? edited.attachmentModule : "";
        save();
    }
    public void select(String alias, String spec) {
        if (!BuilderVariables.validSelection(catalog, alias, spec)) return;
        if (spec.isEmpty()) options.variables.remove(alias);
        else options.variables.put(alias, spec);
        save();
    }
    private void save() {
        BuilderToolSettings.write(tool, options);
        owner.inventory.markDirty();
        detectAndSendChanges();
    }
}
