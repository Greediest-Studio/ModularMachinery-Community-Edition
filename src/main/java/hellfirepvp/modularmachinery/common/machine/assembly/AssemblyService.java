package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.AeAssemblyFactory;
import hellfirepvp.modularmachinery.common.integration.mmcecomplement.AttachmentModuleCompat;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class AssemblyService {
    private AssemblyService() {}

    public static boolean cancelPlayerTasks(EntityPlayer player) {
        return MachineAssemblyManager.cancelPlayer(player, true);
    }

    public static void start(EntityPlayer player, BlockPos pos, AssemblyOptions options) {
        World world = player.world;
        if (world.isRemote) return;
        if (!(world.getTileEntity(pos) instanceof TileMultiblockMachineController controller)) {
            AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.no_controller"); return;
        }
        if (MachineAssemblyManager.checkMachineExist(world, pos)) {
            AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.already_running"); return;
        }
        DynamicMachine machine = controller.getBlueprintMachine();
        Block block = world.getBlockState(pos).getBlock();
        if (machine == null && block instanceof BlockController ctrl) machine = ctrl.getParentMachine();
        if (machine == null && block instanceof BlockFactoryController ctrl) machine = ctrl.getParentMachine();
        if (machine == null) { AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.no_machine"); return; }
        EnumFacing facing = controller.getControllerRotation();
        if (facing == null || !facing.getAxis().isHorizontal()) facing = EnumFacing.NORTH;
        BlockArray selected = null;
        if (!options.attachmentModule.isEmpty() && Mods.MMCE_COMPLEMENT.isPresent()) {
            selected = AttachmentModuleCompat.findPattern(machine, options.attachmentModule);
            if (selected == null) { AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.no_attachment"); return; }
        }
        boolean attachment = selected != null;
        if (selected == null) selected = machine.getPattern();
        BlockArray cached = attachment ? null : BlockArrayCache.getBlockArrayCache(selected, facing);
        BlockArray pattern = new BlockArray(cached == null ? selected : cached);
        if (cached == null) for (EnumFacing rotation = EnumFacing.NORTH; rotation != facing; rotation = rotation.rotateYCCW()) pattern = pattern.rotateYCCW();
        if (!attachment) AssemblyUtils.appendDynamicPatterns(machine, pattern, facing, options.dynamicLength);
        BuilderVariables.apply(pattern, options.variables);
        pattern.getPattern().remove(BlockPos.ORIGIN);
        if (options.disassemble) {
            MachineAssemblyManager.addTask(new MachineDisassemblyTask(world, pos, player, pattern, options));
            AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.disassembly_started");
            return;
        }
        boolean skippedExisting = options.skipExisting && pattern.getPattern().entrySet().removeIf(entry -> !world.isAirBlock(pos.add(entry.getKey())));
        pattern.flushTileBlocksCache();
        StructureIngredient ingredient = StructureIngredient.of(world, pos, pattern);
        boolean skippedNbt = !options.advanced && AssemblyConfig.skipBlockContainNBT && ingredient.itemIngredient().removeIf(entry -> entry.nbt() != null);
        if (!options.advanced && !player.isCreative()) {
            boolean complete = MachineAssembly.checkAllItems(player, ingredient.copy());
            if (!complete && AssemblyConfig.needAllBlocks) return;
        }
        MachineAssembly task = options.advanced && !player.isCreative() && Mods.AE2.isPresent()
            && (options.useAeItems || options.useAeFluids || options.craftMissing)
            ? AeAssemblyFactory.create(world, pos, player, ingredient, options)
            : new MachineAssembly(world, pos, player, ingredient, options);
        MachineAssemblyManager.addTask(task);
        if (skippedExisting || skippedNbt) task.missingMaterials.markSkipped();
        AssemblyUtils.sendTranslation(player, "message.modularmachinery.builder.started");
    }
}
