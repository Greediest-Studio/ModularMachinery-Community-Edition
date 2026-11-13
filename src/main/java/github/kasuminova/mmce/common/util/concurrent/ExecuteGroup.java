package github.kasuminova.mmce.common.util.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ExecuteGroup {
    private static final AtomicLong GROUP_COUNTER = new AtomicLong(0);

    private final long                  groupId;
    private final Queue<ActionExecutor> executors = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean submitted = new AtomicBoolean(false);
    private final AtomicBoolean pending   = new AtomicBoolean(false);

    public ExecuteGroup(final long groupId) {
        this.groupId = groupId;
    }

    public static long newGroupId() {
        return GROUP_COUNTER.getAndIncrement();
    }

    public boolean isEmpty() {
        return executors.isEmpty();
    }

    public ActionExecutor offer(final ActionExecutor executor) {
        executors.add(executor);
        return executor;
    }

    public ActionExecutor poll() {
        return executors.poll();
    }

    public boolean isSubmitted() {
        return submitted.get();
    }

    public void setSubmitted(final boolean submitted) {
        this.submitted.set(submitted);
    }

    public boolean tryClaimSubmit() {
        return submitted.compareAndSet(false, true);
    }

    public boolean markPending() {
        return pending.compareAndSet(false, true);
    }

    public void clearPending() {
        pending.set(false);
    }

    public long getGroupId() {
        return groupId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(groupId);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ExecuteGroup executeGroup && groupId == executeGroup.groupId;
    }
}
