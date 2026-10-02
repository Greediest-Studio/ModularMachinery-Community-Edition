package ink.ikx.mmce.common.assembly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MachineAssemblyManager {

    private static final Map<World, Map<BlockPos, MachineAssembly>> MACHINE_ASSEMBLY_MAP = new IdentityHashMap<>();

    public static void addMachineAssembly(MachineAssembly machineAssembly) {
        MACHINE_ASSEMBLY_MAP.computeIfAbsent(machineAssembly.getWorld(), world -> new HashMap<>())
            .put(machineAssembly.getCtrlPos(), machineAssembly);
    }

    public static boolean checkMachineExist(World world, BlockPos ctrlPos) {
        Map<BlockPos, MachineAssembly> assemblies = MACHINE_ASSEMBLY_MAP.get(world);
        return assemblies != null && assemblies.containsKey(ctrlPos);
    }

    public static Collection<MachineAssembly> getMachineAssemblyListFromPlayer(EntityPlayer player) {
        return MACHINE_ASSEMBLY_MAP.values().stream()
                                   .flatMap(assemblies -> assemblies.values().stream())
                                   .filter(assembly -> player.getGameProfile().getId().equals(
                                       assembly.getPlayer().getGameProfile().getId()))
                                   .collect(Collectors.toList());
    }

    public static void removeMachineAssembly(World world, BlockPos ctrlPos) {
        Map<BlockPos, MachineAssembly> assemblies = MACHINE_ASSEMBLY_MAP.get(world);
        if (assemblies == null) return;
        assemblies.remove(ctrlPos);
        if (assemblies.isEmpty()) MACHINE_ASSEMBLY_MAP.remove(world);
    }

    public static void removeMachineAssembly(EntityPlayer player) {
        MACHINE_ASSEMBLY_MAP.values().forEach(assemblies ->
            assemblies.values().removeIf(assembly -> assembly.getPlayer().equals(player)));
        MACHINE_ASSEMBLY_MAP.values().removeIf(Map::isEmpty);
    }

}
