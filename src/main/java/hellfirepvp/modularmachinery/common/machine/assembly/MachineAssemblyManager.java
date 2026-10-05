package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

/** 木棍、工具、拆卸共用的任务表；只在服务端主线程访问。 */
public final class MachineAssemblyManager {
    private static final Map<World, Map<BlockPos, BuildTask>> TASKS = new IdentityHashMap<>();
    private static final Map<BuildTask, Long> NEXT_TICK = new IdentityHashMap<>();

    public static boolean addTask(BuildTask task) {
        return TASKS.computeIfAbsent(task.getWorld(), w -> new HashMap<>()).putIfAbsent(task.getCtrlPos(), task) == null;
    }
    public static boolean checkMachineExist(World world, BlockPos pos) {
        return TASKS.containsKey(world) && TASKS.get(world).containsKey(pos);
    }
    public static void tick(EntityPlayer player) {
        Map<BlockPos, BuildTask> worldTasks = TASKS.get(player.world);
        if (worldTasks == null) return;
        for (BuildTask task : new ArrayList<>(worldTasks.values())) {
            if (!task.getPlayer().getUniqueID().equals(player.getUniqueID())) continue;
            boolean abort = task.isControllerInvalid() || task.isCancelled();
            if (!abort) {
                long now = player.world.getTotalWorldTime();
                if (now < NEXT_TICK.getOrDefault(task, 0L)) continue;
                NEXT_TICK.put(task, now + Math.max(1, task.getTickInterval()));
                try {
                    task.beginBatch();
                    for (int i = 0; i < task.getOperationsPerTick() && !task.isCompleted() && !task.isCancelled(); i++) task.tick();
                } catch (RuntimeException error) {
                    ModularMachinery.log.error("自动搭建任务执行失败", error);
                    task.cancel();
                    abort = true;
                } finally { task.endBatch(); }
            }
            abort |= task.isCancelled();
            if (abort || task.isCompleted()) {
                if (abort) task.cancel();
                remove(task);
                task.report();
                AssemblyUtils.sendTranslation(player, abort ? task.getCancelledMessageKey() : task.getSuccessMessageKey());
            }
        }
    }
    private static void remove(BuildTask task) {
        Map<BlockPos, BuildTask> tasks = TASKS.get(task.getWorld());
        if (tasks != null) {
            tasks.remove(task.getCtrlPos());
            if (tasks.isEmpty()) TASKS.remove(task.getWorld());
        }
        NEXT_TICK.remove(task);
    }
    public static boolean cancelPlayer(EntityPlayer player, boolean report) {
        boolean cancelled = false;
        List<BuildTask> snapshot = new ArrayList<>();
        TASKS.values().forEach(tasks -> snapshot.addAll(tasks.values()));
        for (BuildTask task : snapshot) {
            if (!task.getPlayer().getUniqueID().equals(player.getUniqueID())) continue;
            task.cancel(); remove(task); cancelled = true;
            if (report) { task.report(); AssemblyUtils.sendTranslation(player, task.getCancelledMessageKey()); }
        }
        return cancelled;
    }
    public static void clearWorld(World world) {
        Map<BlockPos, BuildTask> tasks = TASKS.remove(world);
        if (tasks != null) for (BuildTask task : tasks.values()) { task.cancel(); NEXT_TICK.remove(task); }
    }
    public static void clear() {
        for (World world : new ArrayList<>(TASKS.keySet())) clearWorld(world);
    }
}
