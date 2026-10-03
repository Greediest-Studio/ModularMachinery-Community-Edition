// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementScrubber;
import hellfirepvp.modularmachinery.common.event.machine.*;
import hellfirepvp.modularmachinery.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.event.recipe.RecipeFailureEvent;
import hellfirepvp.modularmachinery.common.event.recipe.FactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.event.recipe.RecipeFailureEvent;
import hellfirepvp.modularmachinery.common.event.recipe.FactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.common.integration.nuclearcraft.InterdimensionalChunkPos;
import hellfirepvp.modularmachinery.common.integration.nuclearcraft.ScrubbedChunksCache;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentScrubberProvider;
import hellfirepvp.modularmachinery.common.tiles.base.AbstractSnapshotMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTileNotifiable;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class TileScrubberProvider extends AbstractSnapshotMachineComponent<RequirementScrubber> implements MachineComponentTileNotifiable {
    // 只在主线程维护。并行配方分别持有覆盖，结束时只释放自己的引用。
    private final Map<RecipeCraftingContext, Integer> activeRecipes = new IdentityHashMap<>();
    private final List<InterdimensionalChunkPos> scrubbedChunks = new ArrayList<>();
    private int currentChunkRange = -1;

    @Override
    protected CraftCheck checkSnapshot(RequirementScrubber requirement) {
        return CraftCheck.success();
    }

    @Override
    protected void updateSnapshot() {
    }

    public void activate(RecipeCraftingContext context, int range) {
        if (isInvalid()) return;
        // 同一配方的多个净化需求合并为最大的同心范围。
        Integer previous = activeRecipes.get(context);
        if (previous == null || previous < range) {
            activeRecipes.put(context, range);
            refreshCoverage();
        }
    }

    public void deactivate(RecipeCraftingContext context) {
        if (activeRecipes.remove(context) != null) refreshCoverage();
    }

    private void refreshCoverage() {
        int range = activeRecipes.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
        if (range == currentChunkRange) return;
        ScrubbedChunksCache.removeScrubbedChunks(scrubbedChunks);
        scrubbedChunks.clear();
        currentChunkRange = range;
        if (range < 0) return;
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (int x = chunkX - range; x <= chunkX + range; x++) {
            for (int z = chunkZ - range; z <= chunkZ + range; z++) {
                scrubbedChunks.add(InterdimensionalChunkPos.of(world.provider.getDimension(), ChunkPos.asLong(x, z)));
            }
        }
        ScrubbedChunksCache.addChunksToCache(scrubbedChunks);
    }

    public void clearScrubbedChunks() {
        activeRecipes.clear();
        refreshCoverage();
    }

    @Override
    public MachineComponentScrubberProvider provideComponent() {
        return new MachineComponentScrubberProvider(IOType.INPUT, this);
    }

    @Override
    public void invalidate() {
        clearScrubbedChunks();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        clearScrubbedChunks();
        super.onChunkUnload();
    }

    @Override
    public void onMachineEvent(MachineEvent event) {
        if (event instanceof RecipeFailureEvent || event instanceof FactoryRecipeFailureEvent) {
            ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
                deactivate(((RecipeEvent) event).getContext());
                return true;
            });
            return;
        }
        if (event instanceof RecipeFailureEvent || event instanceof FactoryRecipeFailureEvent) {
            ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
                deactivate(((RecipeEvent) event).getContext());
                return true;
            });
            return;
        }
        if (event instanceof MachineNotFormedEvent || event instanceof MachineControllerInvalidatedEvent
                || event instanceof MachineControllerRedstoneAffectedEvent && ((MachineControllerRedstoneAffectedEvent) event).isPowered) {
            ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
                activeRecipes.keySet().removeIf(context -> context.getMachineController() == event.getController());
                refreshCoverage();
                return true;
            });
        }
        // 红石恢复后由下一次配方 tick 激活，避免空闲机器恢复旧范围。
    }
}
