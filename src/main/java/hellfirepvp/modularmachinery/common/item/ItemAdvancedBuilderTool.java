package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ItemAdvancedBuilderTool extends Item {
    public ItemAdvancedBuilderTool() {
        setRegistryName(ModularMachinery.MODID, "advanced_builder_tool");
        setTranslationKey("modularmachinery.advanced_builder_tool");
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
        setMaxStackSize(1);
    }
    @Nonnull @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (!world.isRemote) {
            if (player.isSneaking()) AssemblyService.cancelPlayerTasks(player);
            else player.openGui(ModularMachinery.instance, CommonProxy.GuiType.BUILDER_TOOL.ordinal(), world,
                hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : 40, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
    @Nonnull @Override
    public EnumActionResult onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) return onItemRightClick(world, player, hand).getType();
        if (!world.isRemote && !AssemblyService.cancelPlayerTasks(player)) {
            AssemblyOptions options = BuilderToolSettings.read(player.getHeldItem(hand));
            options.hand = hand;
            AssemblyService.start(player, pos, options);
        }
        return EnumActionResult.SUCCESS;
    }
    @Override public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float x, float y, float z, @NotNull EnumHand hand) {
        return onItemUse(player, world, pos, hand, facing, x, y, z);
    }
    @Override public boolean doesSneakBypassUse(@NotNull ItemStack stack, net.minecraft.world.@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityPlayer player) { return false; }
    @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, java.util.List<String> tooltip, net.minecraft.client.util.@NotNull ITooltipFlag flag) {
        tooltip.add(net.minecraft.client.resources.I18n.format("tooltip.modularmachinery.builder.1"));
        tooltip.add(net.minecraft.client.resources.I18n.format("tooltip.modularmachinery.builder.6"));
        tooltip.add(net.minecraft.client.resources.I18n.format("tooltip.modularmachinery.builder.4", BuilderToolSettings.read(stack).dynamicLength));
    }
}
