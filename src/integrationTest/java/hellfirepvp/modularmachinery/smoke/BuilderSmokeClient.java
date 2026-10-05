package hellfirepvp.modularmachinery.smoke;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.GuiBuilderTool;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.MachineLoader;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import hellfirepvp.modularmachinery.common.network.*;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.*;
import java.io.File;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public final class BuilderSmokeClient {
    private int stage, ticks;
    private volatile String failure;
    private int oldWindow;
    public static void install() { MinecraftForge.EVENT_BUS.register(new BuilderSmokeClient()); }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) throws Exception {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (failure != null) { finish(mc, "FAIL: " + failure); return; }
        if (stage == 0 && mc.currentScreen instanceof GuiMainMenu) {
            stage = 1;
            mc.launchIntegratedServer("builder-gui", "Builder GUI", new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.FLAT));
        } else if (stage == 1 && mc.player != null && mc.getIntegratedServer() != null) {
            stage = 2;
            mc.getIntegratedServer().addScheduledTask(() -> {
                if (mc.getIntegratedServer().getPlayerList().getPlayers().isEmpty()) { stage = 1; return; }
                net.minecraft.entity.player.EntityPlayerMP player = mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                java.util.List<IBlockStateDescriptor> states = new java.util.ArrayList<>();
                states.add(IBlockStateDescriptor.of(Blocks.WOOL));
                states.add(IBlockStateDescriptor.of(Blocks.STAINED_GLASS));
                states.add(IBlockStateDescriptor.of(Blocks.STAINED_HARDENED_CLAY));
                states.add(IBlockStateDescriptor.of(Blocks.CONCRETE));
                for (int i = 0; i < 7; i++) MachineLoader.VARIABLE_CONTEXT.put("builder_smoke_" + i, new BlockArray.BlockInformation(states));
                ItemStack main = new ItemStack(ItemsMM.advancedBuilderTool);
                AssemblyOptions separate = new AssemblyOptions(); separate.dynamicLength = 9;
                BuilderToolSettings.write(main, separate);
                player.inventory.setInventorySlotContents(0, main);
                player.inventory.setInventorySlotContents(40, new ItemStack(ItemsMM.advancedBuilderTool));
                player.openGui(ModularMachinery.instance, CommonProxy.GuiType.BUILDER_TOOL.ordinal(), player.world, 40, 0, 0);
            });
        } else if (stage >= 2 && mc.currentScreen instanceof GuiBuilderTool) {
            GuiBuilderTool gui = (GuiBuilderTool) mc.currentScreen;
            if (!gui.getContainer().ready || ++ticks < 25) return;
            ticks = 0;
            ScreenShotHelper.saveScreenshot(new File("."), "builder-" + stage + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            if (stage == 4) {
                oldWindow = gui.getContainer().windowId;
                AssemblyOptions options = new AssemblyOptions(); options.dynamicLength = 3; options.skipExisting = true;
                ModularMachinery.NET_CHANNEL.sendToServer(new PktBuilderConfig(oldWindow, BuilderToolSettings.write(options)));
                ModularMachinery.NET_CHANNEL.sendToServer(new PktBuilderVariable(oldWindow, "builder_smoke_0", "minecraft:wool@5"));
                gui.acceptGhost(new ItemStack(Blocks.GLASS));
                stage = 5;
                return;
            }
            if (stage == 5) {
                stage = 6;
                mc.getIntegratedServer().addScheduledTask(() -> {
                    try {
                        net.minecraft.entity.player.EntityPlayerMP player = mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                        AssemblyOptions off = BuilderToolSettings.read(player.getHeldItemOffhand());
                        check(off.dynamicLength == 3 && off.skipExisting, "副手设置包未保存");
                        check("minecraft:wool@5".equals(off.variables.get("builder_smoke_0")), "别名设置包未保存");
                        check("minecraft:glass@0".equals(off.variables.get(BuilderVariables.ALL)), "Ghost 方块未保存");
                        check(BuilderToolSettings.read(player.inventory.getStackInSlot(0)).dynamicLength == 9, "修改了另一把工具");
                        player.closeContainer();
                        player.openGui(ModularMachinery.instance, CommonProxy.GuiType.BUILDER_TOOL.ordinal(), player.world, 0, 0, 0);
                    } catch (Throwable error) { failure = error.toString(); }
                });
                return;
            }
            if (stage == 6) {
                if (gui.getContainer().windowId == oldWindow) return;
                AssemblyOptions stale = new AssemblyOptions(); stale.dynamicLength = 88;
                ModularMachinery.NET_CHANNEL.sendToServer(new PktBuilderConfig(oldWindow, BuilderToolSettings.write(stale)));
                stage = 7; return;
            }
            if (stage == 7) {
                stage = 8;
                mc.getIntegratedServer().addScheduledTask(() -> {
                    try {
                        net.minecraft.entity.player.EntityPlayerMP player = mc.getIntegratedServer().getPlayerList().getPlayers().get(0);
                        check(BuilderToolSettings.read(player.inventory.getStackInSlot(0)).dynamicLength == 9, "旧窗口配置包修改了新工具");
                        mc.addScheduledTask(() -> finish(mc, "PASS: GUI, server catalog, offhand, per-tool settings, alias selection, ghost selection, stale-window rejection"));
                    } catch (Throwable error) { failure = error.toString(); }
                });
                return;
            }
            if (stage > 7) return;
            Field page = GuiBuilderTool.class.getDeclaredField("page"); page.setAccessible(true); page.setInt(gui, stage == 2 ? 1 : 2);
            if (stage == 3 && !gui.getContainer().catalog.isEmpty()) {
                Field alias = GuiBuilderTool.class.getDeclaredField("alias"); alias.setAccessible(true);
                alias.set(gui, gui.getContainer().catalog.keySet().iterator().next());
            }
            gui.refresh(); stage++;
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void finish(Minecraft mc, String result) {
        try { java.nio.file.Files.write(java.nio.file.Paths.get("builder-smoke-result.txt"), java.util.Collections.singletonList(result), java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception error) { error.printStackTrace(); }
        mc.shutdown();
    }
}
