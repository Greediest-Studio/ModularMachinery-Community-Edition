// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ChunksReader {

    private static ChunksReader instance;

    public static ChunksReader getInstance() {
        if (instance != null) return instance;

        instance = new ChunksReader();
        return instance;
    }

    private ChunksReader() {
    }

    public List<Chunk> getSurroundingChunks(World world, BlockPos pos, int range) {
        List<Chunk> chunks = new ArrayList<>();

        // Calculate the chunk coordinates of the position `pos`
        int chunkX = pos.getX() >> 4; // Convert block coordinates to chunk coordinates
        int chunkZ = pos.getZ() >> 4;

        // Iterate through the surrounding chunks based on the given range
        for (int x = chunkX - range; x <= chunkX + range; x++) {
            for (int z = chunkZ - range; z <= chunkZ + range; z++) {
                // Get the chunk at the current coordinates and add it to the list
                Chunk chunk = world.getChunk(x, z);
                chunks.add(chunk);
            }
        }

        return chunks;
    }
}
