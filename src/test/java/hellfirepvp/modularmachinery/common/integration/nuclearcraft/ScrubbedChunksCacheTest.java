package hellfirepvp.modularmachinery.common.integration.nuclearcraft;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ScrubbedChunksCacheTest {
    @Test
    void removingOneScrubberPreservesAnotherAndDimensionsStaySeparate() {
        InterdimensionalChunkPos first = new InterdimensionalChunkPos(101, 7L);
        InterdimensionalChunkPos otherDimension = new InterdimensionalChunkPos(102, 7L);
        try {
            ScrubbedChunksCache.addChunksToCache(Collections.singletonList(first));
            ScrubbedChunksCache.addChunksToCache(Collections.singletonList(first));
            assertTrue(ScrubbedChunksCache.isChunkScrubbed(first));
            assertFalse(ScrubbedChunksCache.isChunkScrubbed(otherDimension));
            ScrubbedChunksCache.removeScrubbedChunks(Collections.singletonList(first));
            assertTrue(ScrubbedChunksCache.isChunkScrubbed(first));
            ScrubbedChunksCache.removeScrubbedChunks(Collections.singletonList(first));
            assertFalse(ScrubbedChunksCache.isChunkScrubbed(first));
            assertFalse(ScrubbedChunksCache.getInformation().contains(first.toString()));
        } finally {
            ScrubbedChunksCache.removeScrubbedChunks(Collections.singletonList(first));
        }
    }
}
