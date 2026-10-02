package github.kasuminova.mmce.common.world;

import com.github.bsideup.jabel.Desugar;
import github.kasuminova.mmce.common.tile.MEPatternMirrorImage;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.util.concurrent.ExecuteGroup;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MachineComponentManager {
    public static final MachineComponentManager                  INSTANCE     = new MachineComponentManager();
    private final       Map<World, Map<BlockPos, ComponentInfo>> componentMap = new ConcurrentHashMap<>();
    private final Map<TileMultiblockMachineController, Long> pendingGroups = new IdentityHashMap<>();

    private MachineComponentManager() {
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static Pair<BlockPos, TileEntity> getResult(TileEntity component, World world) {
        BlockPos pos = component.getPos();
        TileEntity te = component;

        if (component instanceof MEPatternMirrorImage mepi) {
            if (mepi.providerPos != null) {
                TileEntity tileEntity = world.getTileEntity(mepi.providerPos);
                if (tileEntity instanceof MEPatternProvider mep) {
                    te = mep;
                    pos = mep.getPos();
                }
            }
        }
        return Pair.of(pos, te);
    }

    public synchronized void addWorld(World world) {
        componentMap.putIfAbsent(world, new ConcurrentHashMap<>());
    }

    public synchronized void removeWorld(World world) {
        pendingGroups.keySet().removeIf(ctrl -> ctrl.getWorld() == world);
        Map<BlockPos, ComponentInfo> removed = componentMap.remove(world);
        if (removed == null) {
            return;
        }
        for (final ComponentInfo info : removed.values()) {
            info.owners.clear();
        }
        removed.clear();
    }

    public synchronized void checkComponentShared(TileEntity component, TileMultiblockMachineController ctrl) {
        World world = component.getWorld();
        BlockPos pos;
        TileEntity te;

        if (Loader.isModLoaded("appliedenergistics2")) {
            Pair<BlockPos, TileEntity> result = getResult(component, world);
            pos = result.getLeft();
            te = result.getRight();
        } else {
            te = component;
            pos = component.getPos();
        }

        Map<BlockPos, ComponentInfo> posComponentMap = componentMap.computeIfAbsent(world, v -> new ConcurrentHashMap<>());

        ComponentInfo info = posComponentMap.get(pos);
        if (info == null || !info.areTileEntityEquals(te)) {
            posComponentMap.put(pos, new ComponentInfo(te, pos, new ReferenceOpenHashSet<>(Collections.singleton(ctrl))));
            return;
        }
        Set<TileMultiblockMachineController> owners = info.owners;
        if (!owners.add(ctrl) || owners.size() <= 1) {
            return;
        }

        Set<Long> oldGroups = new HashSet<>();
        owners.forEach(owner -> oldGroups.add(effectiveGroup(owner)));
        if (oldGroups.size() == 1 && !oldGroups.contains(-1L)) {
            return;
        }
        oldGroups.remove(-1L);
        long groupId = oldGroups.isEmpty() ? ExecuteGroup.newGroupId() : Collections.min(oldGroups);
        Set<TileMultiblockMachineController> merged = new ReferenceOpenHashSet<>(owners);
        // Merge every member of both old groups, not only owners of the connecting component.
        for (ComponentInfo componentInfo : posComponentMap.values()) {
            for (TileMultiblockMachineController owner : componentInfo.owners) {
                if (oldGroups.contains(effectiveGroup(owner))) merged.add(owner);
            }
        }
        merged.forEach(owner -> pendingGroups.put(owner, groupId));
    }

    private long effectiveGroup(TileMultiblockMachineController ctrl) {
        return pendingGroups.getOrDefault(ctrl, ctrl.getExecuteGroupId());
    }

    public synchronized boolean hasPendingGroup(TileMultiblockMachineController ctrl) {
        return pendingGroups.containsKey(ctrl);
    }

    /** Called after the current executor batch has drained, before the next batch is scheduled. */
    public synchronized void applyPendingGroups() {
        pendingGroups.forEach((ctrl, group) -> ctrl.setExecuteGroupId(group));
        pendingGroups.clear();
    }

    public synchronized void removeOwner(TileEntity component, TileMultiblockMachineController ctrl) {
        World world = component.getWorld();
        BlockPos pos;
        TileEntity te;

        if (Loader.isModLoaded("appliedenergistics2")) {
            Pair<BlockPos, TileEntity> result = getResult(component, world);
            pos = result.getLeft();
            te = result.getRight();
        } else {
            te = component;
            pos = component.getPos();
        }

        Map<BlockPos, ComponentInfo> posComponentMap = componentMap.computeIfAbsent(world, v -> new ConcurrentHashMap<>());

        ComponentInfo info = posComponentMap.get(pos);
        if (info == null) {
            return;
        }

        if (!info.areTileEntityEquals(te)) {
            ComponentInfo newInfo = new ComponentInfo(te, pos, new ObjectArraySet<>());
            posComponentMap.put(pos, newInfo);
        } else {
            Set<TileMultiblockMachineController> owners = info.owners;
            synchronized (owners) {
                owners.remove(ctrl);
            }
        }
    }

    @Desugar
    public record ComponentInfo(TileEntity te, BlockPos pos, Set<TileMultiblockMachineController> owners) {

        public boolean areTileEntityEquals(TileEntity te) {
            return this.te == te;
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ComponentInfo componentInfo) {
                return pos.equals(componentInfo.pos);
            }
            return false;
        }
    }
}
