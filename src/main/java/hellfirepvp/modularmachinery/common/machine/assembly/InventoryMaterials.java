package hellfirepvp.modularmachinery.common.machine.assembly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

/** 在物品副本上结算流体，再把能力返回的容器写回槽位。 */
public final class InventoryMaterials {
    private InventoryMaterials() {}

    public static MaterialPayment takeItem(EntityPlayer player, ItemStack required) {
        List<ItemStack> inventory = player.inventory.mainInventory;
        int available = 0;
        for (ItemStack stack : inventory) if (matches(stack, required)) available += stack.getCount();
        if (available < required.getCount()) return null;
        int remaining = required.getCount();
        for (ItemStack stack : inventory) {
            if (!matches(stack, required)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
            if (remaining == 0) break;
        }
        player.inventory.markDirty();
        return new MaterialPayment(() -> AssemblyUtils.giveOrDrop(player, required.copy()));
    }

    private static boolean matches(ItemStack a, ItemStack b) {
        return !a.isEmpty() && ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static MaterialPayment takeFluid(EntityPlayer player, FluidStack required) {
        List<ItemStack> inventory = player.inventory.mainInventory;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack original = inventory.get(slot);
            // 与本体原有自动搭建保持一致：桶不作为供料容器。
            if (original.getItem() instanceof net.minecraftforge.fluids.UniversalBucket
                || original.getItem() == net.minecraft.init.Items.WATER_BUCKET
                || original.getItem() == net.minecraft.init.Items.LAVA_BUCKET) continue;
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(original.copy());
            if (handler == null) continue;
            FluidStack simulated = handler.drain(required.copy(), false);
            if (simulated == null || !simulated.containsFluid(required)) continue;
            FluidStack drained = handler.drain(required.copy(), true);
            if (drained == null || !drained.containsFluid(required)) continue;
            int paidSlot = slot;
            ItemStack saved = original.copy();
            inventory.set(slot, handler.getContainer());
            player.inventory.markDirty();
            return new MaterialPayment(() -> {
                inventory.set(paidSlot, saved);
                player.inventory.markDirty();
            });
        }
        return null;
    }

    /** 返回完整回收方案；没有足够空间时不改动真实背包。 */
    public static Runnable prepareFluidReturn(EntityPlayer player, FluidStack fluid) {
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> replacements = new ArrayList<>();
        FluidStack remaining = fluid.copy();
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (stack.isEmpty() || stack.getCount() != 1) continue;
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack.copy());
            if (handler == null) continue;
            int filled = handler.fill(remaining.copy(), true);
            if (filled == 0) continue;
            slots.add(slot);
            replacements.add(handler.getContainer());
            remaining.amount -= filled;
            if (remaining.amount <= 0) return () -> {
                for (int i = 0; i < slots.size(); i++) player.inventory.mainInventory.set(slots.get(i), replacements.get(i));
                player.inventory.markDirty();
            };
        }
        return null;
    }
}
