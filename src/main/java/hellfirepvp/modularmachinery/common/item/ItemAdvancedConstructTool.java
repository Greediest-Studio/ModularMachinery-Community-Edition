// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// This item was adapted from a similar item in MMCE
public class ItemAdvancedConstructTool extends Item {

    private final List<BlockPos> selection = new ArrayList<>();

    public ItemAdvancedConstructTool() {
        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.modularmachinery.advancedconstructtool.tooltip"));
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!worldIn.isRemote && player.isCreative() && worldIn.getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile())) {
            IBlockState clicked = worldIn.getBlockState(pos);
            Block block = clicked.getBlock();

            if (block instanceof BlockController) {
                if (selection.size() != 2) {
                    player.sendMessage(new TextComponentTranslation("message.modularmachinery.advancedconstructtool.invalid.selection"));
                    return EnumActionResult.FAIL;
                }

                getBlocksBetween(selection.get(0), selection.get(1), worldIn).forEach(blockPos -> {
                    PlayerStructureSelectionHelper.toggleInSelection(player, blockPos);
                    player.sendMessage(new TextComponentString("Registering block " + worldIn.getBlockState(blockPos).getBlock().getRegistryName().toString()));
                });
                PlayerStructureSelectionHelper.finalizeSelection(clicked.getValue(BlockController.FACING), worldIn, pos, player);

                PlayerStructureSelectionHelper.purgeSelection(player);
                PlayerStructureSelectionHelper.sendSelection(player);
            } else {
                if (selection.size() >= 2) {
                    selection.clear();
                    player.sendMessage(new TextComponentTranslation("message.modularmachinery.advancedconstructtool.cleared"));
                    return EnumActionResult.SUCCESS;
                }

                selection.add(pos);
                player.sendMessage(new TextComponentTranslation("message.modularmachinery.advancedconstructtool.selection"));
            }
        }
        return EnumActionResult.SUCCESS;
    }

    private List<BlockPos> getBlocksBetween(BlockPos pos1, BlockPos pos2, World world) {
        List<BlockPos> blocks = new ArrayList<>();

        // Determine min and max bounds
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Scan all positions within the cuboid
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos current = new BlockPos(x, y, z);
                    Block block = world.getBlockState(current).getBlock();

                    if (!block.equals(Blocks.AIR)) {
                        blocks.add(current);
                    }
                }
            }
        }

        return blocks;
    }
}
