package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BuilderCoreTest {
    @BeforeAll static void bootstrap() { Bootstrap.register(); }
    @AfterEach void clearTasks() { MachineAssemblyManager.clear(); }

    @Test void settingsArePerStackAndIgnoreLegacyTags() {
        ItemStack first = new ItemStack(Blocks.STONE), second = new ItemStack(Blocks.STONE);
        NBTTagCompound old = new NBTTagCompound(); old.setBoolean("mmce_abt_disassemble_mode", true); first.setTagCompound(old);
        assertFalse(BuilderToolSettings.read(first).disassemble);
        AssemblyOptions options = BuilderToolSettings.read(first);
        options.disassemble = true; options.dynamicLength = 12;
        options.variables.put("casing", "minecraft:stone@0");
        BuilderToolSettings.write(first, options);
        ItemStack loaded = new ItemStack(first.writeToNBT(new NBTTagCompound()));
        assertTrue(BuilderToolSettings.read(loaded).disassemble);
        assertEquals(12, BuilderToolSettings.read(loaded).dynamicLength);
        assertEquals("minecraft:stone@0", BuilderToolSettings.read(loaded).variables.get("casing"));
        assertFalse(BuilderToolSettings.read(second).disassemble);
        assertFalse(second.hasTagCompound());
    }

    @Test void identicalCandidatesRetainDistinctAliasesDuringInterningAndCopying() {
        BlockArray.BlockInformation a = alias("a", Blocks.STONE);
        BlockArray.BlockInformation b = alias("b", Blocks.STONE);
        assertNotSame(a.canonicalize(), b.canonicalize());
        Map<String, String> picks = Collections.singletonMap("a", "minecraft:glass@0");
        assertEquals(Blocks.GLASS, a.copy().withBuilderSelections(picks).getSampleState(0).getBlock());
        assertEquals(Blocks.STONE, b.withBuilderSelections(picks).getSampleState(0).getBlock());
        assertEquals(Blocks.STONE, a.getSampleState(0).getBlock());
    }

    @Test void mixedDirectAndAliasGroupsOnlyReplaceTheirOwnCandidates() {
        IBlockStateDescriptor direct = IBlockStateDescriptor.of(Blocks.DIRT);
        IBlockStateDescriptor variable = IBlockStateDescriptor.of(Blocks.STONE);
        BlockArray.BlockInformation info = new BlockArray.BlockInformation(Arrays.asList(direct, variable));
        info.setVariableGroups(Arrays.asList(new BlockVariableGroup(null, Collections.singletonList(direct)), new BlockVariableGroup("a", Collections.singletonList(variable))));
        BlockArray.BlockInformation selected = info.withBuilderSelections(Collections.singletonMap("a", "minecraft:glass@0"));
        assertEquals(Blocks.DIRT, selected.getMatchingStates().get(0).getApplicable().get(0).getBlock());
        assertEquals(Blocks.GLASS, selected.getMatchingStates().get(1).getApplicable().get(0).getBlock());
        assertEquals(Blocks.STONE, info.getMatchingStates().get(1).getApplicable().get(0).getBlock());
    }

    @Test void selectedOrientationFollowsAllFourControllerRotations() {
        BlockArray.BlockInformation info = alias("a", Blocks.STONE);
        net.minecraft.block.state.IBlockState expected = Blocks.FURNACE.getStateFromMeta(2);
        for (int i = 0; i < 4; i++) {
            assertEquals(expected, info.withBuilderSelections(Collections.singletonMap("a", "minecraft:furnace@2")).getSampleState(0));
            info = info.copyRotateYCCW();
            expected = expected.withRotation(Rotation.COUNTERCLOCKWISE_90);
        }
    }

    @Test void globalChoiceWinsAndPlainDefinitionsDoNotBecomeAliases() {
        Map<String, String> picks = new HashMap<>(); picks.put("all", "minecraft:glass@0"); picks.put("a", "minecraft:dirt@0");
        assertEquals(Blocks.GLASS, alias("a", Blocks.STONE).withBuilderSelections(picks).getSampleState(0).getBlock());
        BlockArray.BlockInformation plain = new BlockArray.BlockInformation(Collections.singletonList(IBlockStateDescriptor.of(Blocks.STONE)));
        assertEquals(Blocks.STONE, plain.withBuilderSelections(picks).getSampleState(0).getBlock());
    }

    @Test void taskClaimsAreSharedAndClearingCancelsOwners() {
        StubTask first = new StubTask(), duplicate = new StubTask();
        assertTrue(MachineAssemblyManager.addTask(first));
        assertFalse(MachineAssemblyManager.addTask(duplicate));
        assertTrue(MachineAssemblyManager.checkMachineExist(null, BlockPos.ORIGIN));
        MachineAssemblyManager.clearWorld(null);
        assertTrue(first.cancelled);
        assertFalse(duplicate.cancelled);
        assertFalse(MachineAssemblyManager.checkMachineExist(null, BlockPos.ORIGIN));
        assertTrue(MachineAssemblyManager.addTask(duplicate));
    }

    @Test void paymentRefundIsAppliedOnlyOnce() {
        int[] balance = {0}; MaterialPayment payment = new MaterialPayment(() -> balance[0] += 1000);
        payment.refund(); payment.refund();
        assertEquals(1000, balance[0]);
    }

    @Test void dynamicDefinitionsClampLengthsAndKeepTheSharedPattern() {
        hellfirepvp.modularmachinery.common.machine.DynamicMachine machine = new hellfirepvp.modularmachinery.common.machine.DynamicMachine("builder_dynamic_test");
        DynamicPattern dynamic = new DynamicPattern("line").setMinSize(2).setMaxSize(3)
            .setStructureSizeOffsetStart(new BlockPos(0, 0, 1)).setStructureSizeOffset(new BlockPos(0, 0, 1));
        dynamic.addFaces(EnumSet.of(net.minecraft.util.EnumFacing.NORTH, net.minecraft.util.EnumFacing.SOUTH));
        dynamic.getPattern().addBlock(BlockPos.ORIGIN, alias("wall", Blocks.STONE));
        machine.addDynamicPattern("line", dynamic);
        BlockArrayCache.addBlockArrayCache(dynamic.getPattern(), net.minecraft.util.EnumFacing.NORTH);
        BlockArray minimum = new BlockArray(), maximum = new BlockArray();
        AssemblyUtils.appendDynamicPatterns(machine, minimum, net.minecraft.util.EnumFacing.NORTH, 0);
        AssemblyUtils.appendDynamicPatterns(machine, maximum, net.minecraft.util.EnumFacing.NORTH, 100);
        assertEquals(2, minimum.getPattern().size());
        assertEquals(3, maximum.getPattern().size());
        BuilderVariables.apply(maximum, Collections.singletonMap("wall", "minecraft:glass@0"));
        assertEquals(Blocks.STONE, dynamic.getPattern().getPattern().get(BlockPos.ORIGIN).getSampleState(0).getBlock());
    }

    @Test void selectionValidationRejectsUnknownAliasesAndInvalidMetadata() {
        Map<String, List<String>> catalog = Collections.singletonMap("wall", Collections.singletonList("minecraft:stone@0"));
        assertFalse(BuilderVariables.validSelection(catalog, "unknown", "minecraft:stone@0"));
        assertFalse(BuilderVariables.validSelection(catalog, "wall", "minecraft:glass@0"));
        assertFalse(BuilderVariables.validSelection(catalog, BuilderVariables.ALL, "minecraft:stone@bad"));
        assertTrue(BuilderVariables.validSelection(catalog, BuilderVariables.ALL, "minecraft:glass@0"));
    }

    private static BlockArray.BlockInformation alias(String name, net.minecraft.block.Block block) {
        List<IBlockStateDescriptor> descriptors = Collections.singletonList(IBlockStateDescriptor.of(block));
        BlockArray.BlockInformation info = new BlockArray.BlockInformation(descriptors);
        info.setVariableGroups(Collections.singletonList(new BlockVariableGroup(name, descriptors)));
        return info;
    }
    private static class StubTask implements BuildTask {
        boolean cancelled;
        public World getWorld() { return null; }
        public BlockPos getCtrlPos() { return BlockPos.ORIGIN; }
        public EntityPlayer getPlayer() { return null; }
        public int getTickInterval() { return 1; }
        public int getOperationsPerTick() { return 1; }
        public boolean isControllerInvalid() { return false; }
        public boolean isCompleted() { return false; }
        public void tick() {}
        public void report() {}
        public void cancel() { cancelled = true; }
        public String getCancelledMessageKey() { return ""; }
        public String getSuccessMessageKey() { return ""; }
    }
}
