package github.kasuminova.mmce.common.concurrent;

import github.kasuminova.mmce.common.util.Sides;
import github.kasuminova.mmce.common.util.concurrent.Action;
import github.kasuminova.mmce.common.util.concurrent.ActionExecutor;
import github.kasuminova.mmce.common.util.concurrent.CustomForkJoinWorkerThreadFactory;
import github.kasuminova.mmce.common.util.concurrent.CustomThreadFactory;
import github.kasuminova.mmce.common.util.concurrent.ExecuteGroup;
import github.kasuminova.mmce.common.util.concurrent.Queues;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import io.netty.util.internal.ThrowableUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TaskExecutor {
    public static final int THREAD_COUNT = Math.min(Math.max(Runtime.getRuntime().availableProcessors() / 4, 4), 8);

    // For render.
    public static final int CLIENT_THREAD_COUNT = Math.min(Math.max(Runtime.getRuntime().availableProcessors() / 2, 4), 16);

    public static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
        5000, TimeUnit.MILLISECONDS,
        new PriorityBlockingQueue<>(),
        new CustomThreadFactory("MMCE-TaskExecutor-%s", SidedThreadGroups.SERVER));

    public static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Sides.isClient() ? CLIENT_THREAD_COUNT : THREAD_COUNT,
        new CustomForkJoinWorkerThreadFactory("MMCE-ForkJoinPool-worker-%s"),
        null, true);

    public static long totalExecuted = 0;
    public static long taskUsedTime  = 0;
    public static long totalUsedTime = 0;
    public static long executedCount = 0;

    public static long tickExisted = 0;

    private final Queue<ActionExecutor> submitted = Queues.createConcurrentQueue();

    private final Queue<ActionExecutor>        executors            = Queues.createConcurrentQueue();
    private final Long2ObjectMap<ExecuteGroup> executeGroups        = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final Queue<ExecuteGroup>          pendingExecuteGroups = Queues.createConcurrentQueue();

    private final Queue<ForkJoinTask<?>> forkJoinTasks = Queues.createConcurrentQueue();

    private final Queue<Action>                 mainThreadActions          = Queues.createConcurrentQueue();
    private final Queue<TileEntitySynchronized> requireUpdateTEQueue       = Queues.createConcurrentQueue();
    private final Queue<TileEntitySynchronized> requireMarkNoUpdateTEQueue = Queues.createConcurrentQueue();

    private final TaskSubmitter submitter = new TaskSubmitter();

    private volatile boolean inTick                = false;
    private volatile boolean shouldUseForkJoinPool = false;

    @SuppressWarnings({"BusyWait", "SameParameterValue"})
    private static void loopWait(final long nanos) {
        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < nanos) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void init() {
        THREAD_POOL.prestartAllCoreThreads();
        submitter.start();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.side == Side.CLIENT) {
            return;
        }
        switch (event.phase) {
            case START -> {
                inTick = true;
                submitter.unpark();
            }
            default -> {
                inTick = false;
                tickExisted++;
            }
        }

        int executed = executeActions();
        if (executed > 0) {
            totalExecuted += executed;
            executedCount++;
        }

        executeGroups.clear();
        pendingExecuteGroups.clear();
        checkShouldUseForkJoinPool();
    }

    /**
     * <p>大量任务的前提下，使用阻塞队列会导致 CPU 资源占用的激增，此时我们不再关注优先级，而是考虑尽可能快地提交任务。</p>
     *
     * <p>With a large number of tasks, the use of a blocking queue can lead to a spike in CPU resource usage, at which
     * point we stop focusing on prioritization and think about submitting tasks as fast as possible.</p>
     */
    private void checkShouldUseForkJoinPool() {
        if (tickExisted % 20 != 0) {
            return;
        }

        if (shouldUseForkJoinPool) {
            if (!shouldUseForkJoinPool()) {
                ModularMachinery.log.warn("The thread pool has been re-switched to ThreadPoolExecutor (below the limit of 1500).");
                shouldUseForkJoinPool = false;
            }
            return;
        }
        if (shouldUseForkJoinPool()) {
            ModularMachinery.log.warn("The thread pool has now been replaced with a ForkJoinPool due to too many tasks in a single commit (Limit 1500).");
            shouldUseForkJoinPool = true;
        }
    }

    private boolean shouldUseForkJoinPool() {
        long executedAvgPerExecution = executedCount == 0 ? 0 : totalExecuted / executedCount;
        return executedAvgPerExecution >= 1500;
    }

    /**
     * 正式执行队列内的所有操作。
     *
     * @return 已执行的数量
     */
    public int executeActions() {
        int executed = 0;
        long time = System.nanoTime() / 1000;

        submitTask();
        executed += spinAwaitActionExecutor();
        executed += executeMainThreadActions();

        updateTileEntity();

        // Empty Check
        if (!submitted.isEmpty() || hasPendingAsyncTasks()) {
            executed += executeActions();
        }

        totalUsedTime += System.nanoTime() / 1000 - time;
        return executed;
    }

    private int executeMainThreadActions() {
        int executed = 0;
        if (mainThreadActions.isEmpty()) {
            return executed;
        }

        Action action;
        while ((action = mainThreadActions.poll()) != null) {
            try {
                action.doAction();
            } catch (Throwable e) {
                ModularMachinery.log.warn("An error occurred during synchronous task execution!");
                ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
            }
            executed++;
        }
        return executed;
    }

    private int spinAwaitActionExecutor() {
        int executed = 0;

        ActionExecutor executor;
        while ((executor = submitted.poll()) != null) {
            // Spin up while completing the operation in the queue.
            while (!executor.isCompleted) {
                executed += executeMainThreadActions();
                updateTileEntity();
                if (!executor.isCompleted) {
                    loopWait(100_000L);
                }
            }

            taskUsedTime += executor.usedTime;
            executed++;
        }
        return executed;
    }

    private void updateTileEntity() {
        if (requireUpdateTEQueue.isEmpty() && requireMarkNoUpdateTEQueue.isEmpty()) {
            return;
        }

        TileEntitySynchronized te;
        while ((te = requireUpdateTEQueue.poll()) != null) {
            te.markForUpdate();
        }

        while ((te = requireMarkNoUpdateTEQueue.poll()) != null) {
            te.markNoUpdate();
        }
    }

    /**
     * <p>添加一个异步操作引用，这个操作必定在本 Tick 结束前执行完毕。</p>
     *
     * @param action 要执行的异步任务
     */
    public ActionExecutor addTask(final Action action) {
        return addTask(action, 0);
    }

    /**
     * <p>添加一个异步操作引用，这个操作必定在本 Tick 结束前执行完毕。</p>
     *
     * @param action   要执行的异步任务
     * @param priority 优先级
     */
    public ActionExecutor addTask(final Action action, final int priority) {
        ActionExecutor actionExecutor = new ActionExecutor(action, priority);
        executors.offer(actionExecutor);
        if (inTick) {
            submitter.unparkIfParked();
        }

        return actionExecutor;
    }

    public ActionExecutor addExecuteGroupTask(final Action action, final long groupId) {
        ExecuteGroup group = executeGroups.get(groupId);
        if (group == null) {
            synchronized (executeGroups) {
                group = executeGroups.get(groupId);
                if (group == null) {
                    group = new ExecuteGroup(groupId);
                    executeGroups.put(groupId, group);
                }
            }
        }

        ActionExecutor executor = group.offer(new ActionExecutor(action));
        if (!group.isSubmitted() && group.markPending()) {
            pendingExecuteGroups.offer(group);
            if (inTick) {
                submitter.unparkIfParked();
            }
        }
        return executor;
    }

    public <T> ForkJoinTask<T> submitForkJoinTask(final ForkJoinTask<T> task) {
        forkJoinTasks.offer(task);
        if (inTick) {
            submitter.unparkIfParked();
        }
        return task;
    }

    /**
     * <p>添加一个同步操作引用，这个操作必定会在异步操作完成后在<strong>主线程</strong>中顺序执行。</p>
     *
     * @param action 要执行的同步任务
     */
    public void addSyncTask(final Action action) {
        mainThreadActions.offer(action);
    }

    public void addTEUpdateTask(final TileEntitySynchronized te) {
        requireUpdateTEQueue.offer(te);
    }

    public void addTEMarkNoUpdateTask(final TileEntitySynchronized te) {
        requireMarkNoUpdateTEQueue.offer(te);
    }

    private void execute(final ActionExecutor executor) {
        if (shouldUseForkJoinPool) {
            FORK_JOIN_POOL.execute(executor);
        } else {
            THREAD_POOL.execute(executor);
        }
    }

    private boolean hasPendingAsyncTasks() {
        return !executors.isEmpty() || !pendingExecuteGroups.isEmpty() || !forkJoinTasks.isEmpty();
    }

    private synchronized void submitTask() {
        ActionExecutor executor;
        while ((executor = executors.poll()) != null) {
            execute(executor);
            submitted.offer(executor);
        }

        ForkJoinTask<?> forkJoinTask;
        while ((forkJoinTask = forkJoinTasks.poll()) != null) {
            FORK_JOIN_POOL.submit(forkJoinTask);
        }

        ExecuteGroup group;
        while ((group = pendingExecuteGroups.poll()) != null) {
            group.clearPending();
            if (group.isEmpty() || !group.tryClaimSubmit()) {
                continue;
            }
            final ExecuteGroup targetGroup = group;
            ActionExecutor groupExecutor = new ActionExecutor(() -> {
                try {
                    ActionExecutor actionExecutor;
                    while ((actionExecutor = targetGroup.poll()) != null) {
                        actionExecutor.run();
                    }
                } finally {
                    targetGroup.setSubmitted(false);
                    if (!targetGroup.isEmpty() && targetGroup.markPending()) {
                        pendingExecuteGroups.offer(targetGroup);
                        if (inTick) {
                            submitter.unparkIfParked();
                        }
                    }
                }
            });
            execute(groupExecutor);
            submitted.offer(groupExecutor);
        }
    }

    public class TaskSubmitter implements Runnable {
        public Thread thread = null;
        private volatile boolean parked = false;

        public void start() {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.setName("MMCE-TaskSubmitter");
            thread.start();
        }

        public void unpark() {
            if (thread != null) {
                LockSupport.unpark(thread);
            }
        }

        public void unparkIfParked() {
            if (parked && thread != null) {
                LockSupport.unpark(thread);
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (inTick) {
                    if (hasPendingAsyncTasks()) {
                        submitTask();
                    } else {
                        parked = true;
                        if (!hasPendingAsyncTasks() && inTick) {
                            LockSupport.parkNanos(50_000L);
                        }
                        parked = false;
                    }
                } else {
                    parked = true;
                    if (!inTick) {
                        LockSupport.park();
                    }
                    parked = false;
                }
            }
        }
    }
}
