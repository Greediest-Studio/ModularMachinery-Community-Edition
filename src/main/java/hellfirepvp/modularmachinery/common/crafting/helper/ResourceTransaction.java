package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.concurrent.Sync;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.Predicate;

/** 同一需求的预检和提交共用仓室锁；固定顺序避免多个配方反向取锁。 */
public final class ResourceTransaction {
    private static final Object COLLISION_LOCK = new Object();

    private ResourceTransaction() {}

    /** 扣料和脚本修改器只在副本上结算一次，完整成功后写回有变动的槽位。 */
    public static boolean consumeItems(List<ProcessingComponent<?>> components,
                                       Predicate<List<ProcessingComponent<?>>> operation) {
        return withComponents(components, () -> {
            Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            List<ProcessingComponent<?>> sources = new ArrayList<>();
            for (ProcessingComponent<?> component : components) {
                if (seen.add(component.getProvidedComponent())) sources.add(component);
            }
            List<ProcessingComponent<?>> copies = ItemUtils.copyItemHandlerComponents(sources);
            if (!operation.test(copies)) return false;
            for (int i = 0; i < sources.size(); i++) {
                IItemHandlerModifiable original = (IItemHandlerModifiable) sources.get(i).getProvidedComponent();
                IItemHandlerModifiable copy = (IItemHandlerModifiable) copies.get(i).getProvidedComponent();
                for (int slot = 0; slot < copy.getSlots(); slot++) {
                    ItemStack result = copy.getStackInSlot(slot);
                    if (!ItemStack.areItemStacksEqual(original.getStackInSlot(slot), result)) {
                        original.setStackInSlot(slot, result.copy());
                    }
                }
            }
            return true;
        });
    }

    public static <T> T withComponents(List<ProcessingComponent<?>> components, Supplier<T> operation) {
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Object> locks = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            Object handler = component.getProvidedComponent();
            if (seen.add(handler)) locks.add(handler);
        }
        locks.sort(Comparator.comparingInt(System::identityHashCode));
        for (int i = 1; i < locks.size(); i++) {
            if (System.identityHashCode(locks.get(i - 1)) == System.identityHashCode(locks.get(i))) {
                synchronized (COLLISION_LOCK) {
                    return withLocks(locks, 0, operation);
                }
            }
        }
        return withLocks(locks, 0, operation);
    }

    private static <T> T withLocks(List<Object> locks, int index, Supplier<T> operation) {
        if (index == locks.size()) return operation.get();
        List<T> result = new ArrayList<>(1);
        Sync.executeSyncIfPresent(locks.get(index), () -> result.add(withLocks(locks, index + 1, operation)));
        return result.get(0);
    }
}
