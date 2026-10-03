// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.nuclearcraft;

import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** 每个区块记录覆盖它的净化仓数，重叠范围独立释放。 */
@Mod.EventBusSubscriber(modid = ModularMachinery.MODID)
public final class ScrubbedChunksCache {
    private static final ConcurrentMap<InterdimensionalChunkPos, Integer> CHUNKS = new ConcurrentHashMap<>();

    public static void addChunksToCache(List<InterdimensionalChunkPos> chunks) {
        chunks.forEach(chunk -> CHUNKS.merge(chunk, 1, Integer::sum));
    }

    public static boolean isChunkScrubbed(InterdimensionalChunkPos chunk) {
        return CHUNKS.containsKey(chunk);
    }

    public static void removeScrubbedChunks(List<InterdimensionalChunkPos> chunks) {
        chunks.forEach(chunk -> CHUNKS.computeIfPresent(chunk, (key, count) -> count <= 1 ? null : count - 1));
    }

    public static void clear() {
        CHUNKS.clear();
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            int dimension = event.getWorld().provider.getDimension();
            CHUNKS.keySet().removeIf(chunk -> chunk.dimensionId() == dimension);
        }
    }

    public static String getInformation() {
        StringBuilder result = new StringBuilder();
        CHUNKS.forEach((chunk, count) -> result.append(chunk).append(" -> [total: ").append(count).append("]\n"));
        return result.toString();
    }
}
