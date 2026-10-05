package hellfirepvp.modularmachinery.smoke;

import com.mojang.authlib.GameProfile;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.*;
import hellfirepvp.modularmachinery.common.machine.*;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.event.world.BlockEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Mod(modid = "mmce_builder_smoke", version = "1", dependencies = "required-after:modularmachinery")
public final class BuilderSmoke {
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) BuilderSmokeClient.install();
    }
    @Mod.EventHandler public void started(FMLServerStartedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (!server.isDedicatedServer()) return;
        try {
            verify(server.getWorld(0));
            Files.write(Paths.get("builder-smoke-result.txt"), Collections.singletonList("PASS: registry, four facings, shared tasks, survival, creative, protected placement, inventory drops, fluid recovery"), StandardCharsets.UTF_8);
        } catch (Throwable error) {
            error.printStackTrace();
            try { Files.write(Paths.get("builder-smoke-result.txt"), Collections.singletonList("FAIL: " + error), StandardCharsets.UTF_8); } catch (Exception ignored) {}
        } finally { server.initiateShutdown(); }
    }
    private void verify(WorldServer world) {
        check(ItemsMM.advancedBuilderTool != null, "工具未注册");
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "BuilderSmoke"));
        player.setPosition(0, 100, 0);
        world.getChunk(0, 0);
        for (int x = 0; x < 40; x++) for (int z = 0; z < 16; z++) world.setBlockToAir(new BlockPos(x, 100, z));
        DynamicMachine machine = new DynamicMachine("builder_smoke");
        machine.getPattern().addBlock(new BlockPos(1, 0, 0), new BlockArray.BlockInformation(Arrays.asList(
            IBlockStateDescriptor.of(BlocksMM.blockCasing.getDefaultState()), IBlockStateDescriptor.of(Blocks.IRON_BLOCK))));
        if (hellfirepvp.modularmachinery.common.base.Mods.AE2.isPresent()) BuilderAeSmoke.verify();
        MachineRegistry.registerMachines(Collections.singletonList(machine));
        BlockArrayCache.buildCache(MachineRegistry.getLoadedMachines());
        int index = 0;
        for (net.minecraft.block.Block controllerBlock : new net.minecraft.block.Block[]{BlocksMM.blockController, BlocksMM.blockFactoryController}) for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos pos = new BlockPos(3 + index++ * 4, 100, 3);
            world.getChunk(pos);
            world.setBlockState(pos, controllerBlock.getDefaultState().withProperty(BlockController.FACING, facing));
            TileMultiblockMachineController controller = (TileMultiblockMachineController) world.getTileEntity(pos);
            controller.setControllerRotation(facing);
            ItemStack blueprint = new ItemStack(ItemsMM.blueprint); ItemBlueprint.setAssociatedMachine(blueprint, machine);
            controller.getInventory().setStackInSlot(TileMultiblockMachineController.BLUEPRINT_SLOT, blueprint);
            player.inventory.mainInventory.set(0, new ItemStack(ItemsMM.advancedBuilderTool));
            player.inventory.mainInventory.set(1, new ItemStack(Blocks.IRON_BLOCK));
            AssemblyOptions options = new AssemblyOptions(); options.advanced = true;
            options.useAeItems = options.useAeFluids = options.craftMissing = hellfirepvp.modularmachinery.common.base.Mods.AE2.isPresent();
            AssemblyService.start(player, pos, options);
            check(MachineAssemblyManager.checkMachineExist(world, pos), "未排入任务");
            AssemblyService.start(player, pos, options);
            drive(world, player);
            BlockPos relative = new BlockPos(1, 0, 0);
            for (EnumFacing f = EnumFacing.NORTH; f != facing; f = f.rotateYCCW()) relative = MiscUtils.rotateYCCW(relative);
            BlockPos target = pos.add(relative);
            check(world.getBlockState(target).getBlock() == Blocks.IRON_BLOCK, "方向或搭建错误: " + facing);
            check(count(player, net.minecraft.item.Item.getItemFromBlock(Blocks.IRON_BLOCK)) == 0, "材料未扣除");
            RejectBreak protect = new RejectBreak(target); MinecraftForge.EVENT_BUS.register(protect);
            try {
                options.disassemble = true; AssemblyService.start(player, pos, options); drive(world, player);
                check(world.getBlockState(target).getBlock() == Blocks.IRON_BLOCK, "被保护的方块遭到拆卸");
                check(count(player, net.minecraft.item.Item.getItemFromBlock(Blocks.IRON_BLOCK)) == 0, "取消拆卸仍产生掉落");
            } finally { MinecraftForge.EVENT_BUS.unregister(protect); }
            options.disassemble = true;
            AssemblyService.start(player, pos, options); drive(world, player);
            check(world.isAirBlock(target) && world.getTileEntity(pos) != null, "拆卸范围错误");
            check(count(player, net.minecraft.item.Item.getItemFromBlock(Blocks.IRON_BLOCK)) == 1, "拆卸掉落错误");

            RejectPlace reject = new RejectPlace(target); MinecraftForge.EVENT_BUS.register(reject);
            try {
                options.disassemble = false; AssemblyService.start(player, pos, options); drive(world, player);
                check(world.isAirBlock(target), "被取消的方块仍放置");
                check(count(player, net.minecraft.item.Item.getItemFromBlock(Blocks.IRON_BLOCK)) == 1, "被取消的放置未退款");
            } finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            player.interactionManager.setGameType(net.minecraft.world.GameType.CREATIVE); player.inventory.clear();
            options.disassemble = false; AssemblyService.start(player, pos, options); drive(world, player);
            check(world.getBlockState(target).getBlock() == BlocksMM.blockCasing, "创造模式未采用默认候选");
            player.interactionManager.setGameType(net.minecraft.world.GameType.SURVIVAL);
            world.setBlockToAir(target);
            options.disassemble = false;
            AssemblyService.start(player, pos, options);
            check(AssemblyService.cancelPlayerTasks(player), "无法取消任务");
            check(!MachineAssemblyManager.checkMachineExist(world, pos), "取消后仍占用控制器");
        }
        BlockPos ctrl = new BlockPos(2, 100, 12), chest = ctrl.east();
        world.setBlockState(ctrl, BlocksMM.blockController.getDefaultState());
        world.setBlockState(chest, Blocks.CHEST.getDefaultState());
        ((TileEntityChest) world.getTileEntity(chest)).setInventorySlotContents(0, new ItemStack(Items.DIAMOND, 7));
        net.minecraft.nbt.NBTTagCompound tileOptions = new net.minecraft.nbt.NBTTagCompound();
        tileOptions.setString("id", "minecraft:furnace"); tileOptions.setInteger("x", -200); tileOptions.setString("CustomName", "Builder NBT");
        MachineAssembly.applyTileNbt(world, chest, tileOptions);
        check(world.getTileEntity(chest).getPos().equals(chest) && ((TileEntityChest) world.getTileEntity(chest)).getName().equals("Builder NBT"), "方块 NBT 未应用或覆盖了坐标");
        BlockArray pattern = new BlockArray(); pattern.addBlock(new BlockPos(1, 0, 0), info(Blocks.CHEST));
        AssemblyOptions options = new AssemblyOptions(); options.advanced = true;
        MachineAssemblyManager.addTask(new MachineDisassemblyTask(world, ctrl, player, pattern, options)); drive(world, player);
        check(count(player, Items.DIAMOND) == 7, "容器内容丢失或重复");
        check(count(player, net.minecraft.item.Item.getItemFromBlock(Blocks.CHEST)) == 1, "容器本体掉落重复");

        BlockPos water = ctrl.south(); world.setBlockState(water, Blocks.WATER.getDefaultState());
        BlockArray fluidPattern = new BlockArray(); fluidPattern.addBlock(new BlockPos(0, 0, 1), info(Blocks.WATER));
        player.inventory.clear();
        MachineAssemblyManager.addTask(new MachineDisassemblyTask(world, ctrl, player, fluidPattern, options)); drive(world, player);
        check(world.getBlockState(water).getBlock() == Blocks.WATER, "无回收位置时清除了流体");
        player.inventory.mainInventory.set(0, new ItemStack(ItemsMM.builderFluidReturn));
        MachineAssemblyManager.addTask(new MachineDisassemblyTask(world, ctrl, player, fluidPattern, options)); drive(world, player);
        check(world.isAirBlock(water), "流体拆卸失败");
        check(FluidUtil.getFluidContained(player.inventory.mainInventory.get(0)).amount == 1000, "流体回收量错误");
        world.setBlockState(water, Blocks.WATER.getDefaultState().withProperty(net.minecraft.block.BlockLiquid.LEVEL, 5));
        player.inventory.mainInventory.set(0, new ItemStack(ItemsMM.builderFluidReturn));
        MachineAssemblyManager.addTask(new MachineDisassemblyTask(world, ctrl, player, fluidPattern, options)); drive(world, player);
        check(!world.isAirBlock(water) && FluidUtil.getFluidContained(player.inventory.mainInventory.get(0)) == null, "将流动水复制成了完整水桶");
    }
    private static BlockArray.BlockInformation info(net.minecraft.block.Block block) { return new BlockArray.BlockInformation(Collections.singletonList(IBlockStateDescriptor.of(block))); }
    private static int count(FakePlayer player, net.minecraft.item.Item item) { return player.inventory.mainInventory.stream().filter(s -> s.getItem() == item).mapToInt(ItemStack::getCount).sum(); }
    private static void drive(WorldServer world, FakePlayer player) {
        for (int i = 0; i < 12; i++) { world.getWorldInfo().setWorldTotalTime(world.getTotalWorldTime() + 1); MachineAssemblyManager.tick(player); }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    public static final class RejectPlace {
        private final BlockPos pos;
        RejectPlace(BlockPos pos) { this.pos = pos; }
        @SubscribeEvent public void onPlace(BlockEvent.PlaceEvent event) { if (event.getPos().equals(pos)) event.setCanceled(true); }
    }
    public static final class RejectBreak {
        private final BlockPos pos;
        RejectBreak(BlockPos pos) { this.pos = pos; }
        @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) { if (event.getPos().equals(pos)) event.setCanceled(true); }
    }
}
