package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.common.machine.MachineLoader;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import java.util.*;

public final class BuilderVariables {
    public static final String ALL = "all";
    private BuilderVariables() {}

    public static Map<String, List<String>> catalog() {
        Map<String, List<String>> catalog = new TreeMap<>();
        MachineLoader.VARIABLE_CONTEXT.forEach((name, info) -> {
            Set<String> states = new LinkedHashSet<>();
            info.getMatchingStates().forEach(desc -> {
                for (IBlockState state : desc.getApplicable()) {
                    Block block = state.getBlock();
                    states.add(block.getRegistryName() + "@" + block.getMetaFromState(state));
                }
            });
            catalog.put(name, new ArrayList<>(states));
        });
        return catalog;
    }

    public static boolean validSelection(Map<String, List<String>> catalog, String name, String spec) {
        if (!ALL.equals(name) && !catalog.containsKey(name)) return false;
        if (spec.isEmpty()) return true;
        if (ALL.equals(name)) {
            try { return !BlockArray.BlockInformation.getDescriptor(spec).getApplicable().isEmpty(); }
            catch (RuntimeException invalid) { return false; }
        }
        return catalog.get(name).contains(spec);
    }

    public static void apply(BlockArray pattern, Map<String, String> selections) {
        pattern.getPattern().replaceAll((pos, info) -> info.withBuilderSelections(selections));
        pattern.flushTileBlocksCache();
    }
}
