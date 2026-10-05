// 移植自 MMCE-AdvancedBuilderTool，按 GPL-3.0 许可融合。
package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.common.util.DynamicPattern;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AssemblyUtils {
    // 放置以流体方块为单位（1000 mB）；退款优先填回随身容器，否则给出对应流体桶。
    public static void returnFluid(EntityPlayer player, FluidStack fluid) {
        Runnable fill = InventoryMaterials.prepareFluidReturn(player, fluid);
        if (fill != null) { fill.run(); return; }
        ItemStack bucket = FluidUtil.getFilledBucket(fluid);
        if (!bucket.isEmpty() && fluid.amount == 1000) giveOrDrop(player, bucket);
        else {
            // 非整桶余量保存在工具的能力容器，避免丢弃或向上取整生成流体。
            giveOrDrop(player, hellfirepvp.modularmachinery.common.item.ItemBuilderFluidReturn.create(fluid));
        }
    }


    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void sendTranslation(EntityPlayer player, String key, Object... args) {
        if (player == null) {
            return;
        }
        if (player.world.isRemote) {
            return;
        }
        String fingerprint = buildMessageFingerprint(key, args);
        long now = player.world.getTotalWorldTime();
        String lastFingerprint = player.getEntityData().getString(MessageKeys.LAST_MESSAGE_TAG);
        long lastMessageTick = player.getEntityData().getLong(MessageKeys.LAST_MESSAGE_TICK_TAG);
        if (fingerprint.equals(lastFingerprint) && now - lastMessageTick <= 2) {
            return;
        }
        player.getEntityData().setString(MessageKeys.LAST_MESSAGE_TAG, fingerprint);
        player.getEntityData().setLong(MessageKeys.LAST_MESSAGE_TICK_TAG, now);
        player.sendMessage(new TextComponentTranslation(key, args));
    }

    private static String buildMessageFingerprint(String key, Object... args) {
        StringBuilder builder = new StringBuilder(key);
        if (args != null) {
            for (Object arg : args) {
                builder.append('\u0001').append(arg);
            }
        }
        return builder.toString();
    }

    public static String posToString(BlockPos pos) {
        return hellfirepvp.modularmachinery.common.util.MiscUtils.posToString(pos);
    }

    public static void giveOrDrop(EntityPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        if (player.inventory.addItemStackToInventory(remaining) || remaining.isEmpty()) {
            return;
        }
        player.dropItem(remaining, false);
    }

    public static boolean isReplaceableForAssembly(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        return world.isAirBlock(pos) || block instanceof IPlantable || block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    public static List<IFluidHandlerItem> getFluidHandlerItems(List<ItemStack> inventory) {
        List<IFluidHandlerItem> fluidHandlers = new ArrayList<>();
        for (ItemStack invStack : inventory) {
            Item item = invStack.getItem();
            if (item instanceof UniversalBucket || item == Items.LAVA_BUCKET || item == Items.WATER_BUCKET) {
                continue;
            }
            if (!FluidUtils.isFluidHandler(invStack)) {
                continue;
            }
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(invStack);
            if (handler != null) {
                fluidHandlers.add(handler);
            }
        }
        return fluidHandlers;
    }

    public static void appendDynamicPatterns(DynamicMachine machine, BlockArray machinePattern, EnumFacing controllerFacing, int requestedLength) {
        Map<String, DynamicPattern> dynamicPatterns = machine.getDynamicPatterns();
        if (dynamicPatterns == null || dynamicPatterns.isEmpty()) {
            return;
        }
        for (DynamicPattern pattern : dynamicPatterns.values()) {
            if (pattern.getFaces() == null || pattern.getFaces().isEmpty()) {
                continue;
            }

            int clamped = clamp(requestedLength, pattern.getMinSize(), pattern.getMaxSize());
            for (EnumFacing face : pattern.getFaces()) {
                pattern.addPatternToBlockArray(machinePattern, clamped, face, controllerFacing);
            }
        }
    }

    public static boolean areItemStacksEqual(ItemStack first, ItemStack second) {
        return ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
    }

    public static boolean areFluidsEqual(FluidStack first, FluidStack second) {
        return first != null && second != null && first.isFluidEqual(second);
    }

}
