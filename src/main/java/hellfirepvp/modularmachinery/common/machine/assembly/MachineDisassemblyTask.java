package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.Ae2GridAccess;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.FluidUtils;
import hellfirepvp.modularmachinery.common.util.StructureIngredient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

public final class MachineDisassemblyTask extends MachineAssembly {
    private final Iterator<Map.Entry<BlockPos, BlockArray.BlockInformation>> entries;

    public MachineDisassemblyTask(World world, BlockPos pos, EntityPlayer player, BlockArray pattern, AssemblyOptions options) {
        super(world, pos, player, new StructureIngredient(new ArrayList<>(), new ArrayList<>()), options);
        entries = pattern.getPattern().entrySet().iterator();
    }

    @Override public boolean isCompleted() { return !entries.hasNext(); }
    @Override public String getCancelledMessageKey() { return "message.modularmachinery.builder.disassembly_cancelled"; }
    @Override public String getSuccessMessageKey() {
        return missingMaterials.hasSkipped() ? "message.modularmachinery.builder.partial" : "message.modularmachinery.builder.disassembly_success";
    }

    @Override public void tick() {
        Map.Entry<BlockPos, BlockArray.BlockInformation> entry = entries.next();
        BlockPos pos = ctrlPos.add(entry.getKey());
        if (pos.equals(ctrlPos) || !world.isBlockLoaded(pos) || world.isAirBlock(pos)) return;
        IBlockState state = world.getBlockState(pos);
        if (!entry.getValue().matchesState(world, pos, state)) return;
        if (state.getBlockHardness(world, pos) < 0 || !world.isBlockModifiable(player, pos)
            || !player.canPlayerEdit(pos, net.minecraft.util.EnumFacing.UP, player.getHeldItem(options.hand))) {
            missingMaterials.markSkipped(); return;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) { missingMaterials.markSkipped(); return; }

        FluidStack fluid = FluidUtils.getFluidStackFromBlockState(state);
        if (fluid != null) {
            // 读取方块实际可回收量：流动中的水不能按一整桶回收。
            net.minecraftforge.fluids.capability.IFluidHandler source = net.minecraftforge.fluids.FluidUtil.getFluidHandler(world, pos, null);
            fluid = source == null ? null : source.drain(Integer.MAX_VALUE, false);
            if (fluid == null || fluid.amount <= 0) { missingMaterials.markSkipped(); return; }
            Runnable inventoryReturn = InventoryMaterials.prepareFluidReturn(player, fluid);
            boolean ae = options.useAeFluids && Mods.AE2.isPresent() && Ae2GridAccess.canInsertFluid(player, fluid);
            if (!ae && inventoryReturn == null) {
                missingMaterials.markSkipped();
                AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.no_fluid_destination");
                return;
            }
            FluidStack recovered = source.drain(Integer.MAX_VALUE, true);
            if (recovered == null || recovered.amount <= 0) { missingMaterials.markSkipped(); return; }
            if (ae) {
                FluidStack remaining = Ae2GridAccess.insertFluid(player, recovered);
                if (remaining != null && remaining.amount > 0) AssemblyUtils.returnFluid(player, remaining);
            } else if (recovered.isFluidStackIdentical(fluid)) inventoryReturn.run();
            else AssemblyUtils.returnFluid(player, recovered);
            return;
        }

        // 原生破坏与采收负责容器内容；只收集这次调用生成的掉落物，避免复制能力库存。
        DropCollector collector = new DropCollector(world, pos);
        TileEntity tile = world.getTileEntity(pos);
        MinecraftForge.EVENT_BUS.register(collector);
        try {
            if (!state.getBlock().removedByPlayer(state, world, pos, player, true)) {
                missingMaterials.markSkipped(); return;
            }
            state.getBlock().onPlayerDestroy(world, pos, state);
            state.getBlock().harvestBlock(world, player, pos, state, tile, ItemStack.EMPTY);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(collector);
            for (ItemStack drop : collector.drops) {
                ItemStack remaining = options.useAeItems && Mods.AE2.isPresent() ? Ae2GridAccess.insertItem(player, drop) : drop;
                AssemblyUtils.giveOrDrop(player, remaining);
            }
        }
    }

    public static final class DropCollector {
        private final World world;
        private final BlockPos pos;
        final List<ItemStack> drops = new ArrayList<>();
        DropCollector(World world, BlockPos pos) { this.world = world; this.pos = pos; }
        @SubscribeEvent
        public void onDrop(EntityJoinWorldEvent event) {
            if (event.getWorld() == world && event.getEntity() instanceof EntityItem item && item.getDistanceSq(pos) < 16) {
                drops.add(item.getItem().copy());
                event.setCanceled(true);
            }
        }
    }
}
