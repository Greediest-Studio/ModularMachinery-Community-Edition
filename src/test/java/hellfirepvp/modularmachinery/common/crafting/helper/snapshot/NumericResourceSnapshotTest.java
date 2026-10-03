package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NumericResourceSnapshotTest {
    @Test
    void combinesOnlyUsableAmountsAcrossChunks() {
        Map<Long, Double> amounts = new HashMap<>();
        amounts.put(ChunkPos.asLong(0, 0), 12D);
        amounts.put(ChunkPos.asLong(1, 0), 8D);
        NumericResourceSnapshot<Demand> snapshot = new NumericResourceSnapshot<>(1, BlockPos.ORIGIN, amounts);
        assertTrue(snapshot.canHandleInput(new Demand(1, 10, 5, 15)));
        assertFalse(snapshot.canHandleInput(new Demand(1, 11, 5, 15)));
        assertTrue(snapshot.canHandleOutput(new Demand(1, 10, 5, 15)));
        assertFalse(snapshot.canHandleOutput(new Demand(1, 11, 5, 15)));
    }

    @Test
    void respectsNegativeCoordinatesAndRequestedRange() {
        Map<Long, Double> amounts = new HashMap<>();
        amounts.put(ChunkPos.asLong(-1, -1), 3D);
        amounts.put(ChunkPos.asLong(0, -1), 50D);
        NumericResourceSnapshot<Demand> snapshot = new NumericResourceSnapshot<>(1, new BlockPos(-1, 64, -1), amounts);
        assertFalse(snapshot.canHandleInput(new Demand(0, 4, 0, 100)));
        assertTrue(snapshot.canHandleInput(new Demand(1, 53, 0, 100)));
        assertFalse(snapshot.getSnapshotForRange(0).canHandleInput(new Demand(1, 1, 0, 100)));
    }

    @Test
    void snapshotDoesNotObserveLaterWorldChanges() {
        Map<Long, Double> amounts = new HashMap<>();
        amounts.put(ChunkPos.asLong(0, 0), 5D);
        NumericResourceSnapshot<Demand> snapshot = new NumericResourceSnapshot<>(0, BlockPos.ORIGIN, amounts);
        amounts.put(ChunkPos.asLong(0, 0), 100D);
        assertTrue(snapshot.canHandleInput(new Demand(0, 5, 0, 100)));
        assertFalse(snapshot.canHandleInput(new Demand(0, 6, 0, 100)));
    }

    private static class Demand implements IMultiChunkRequirement {
        private final int range;
        private final double amount, min, max;
        Demand(int range, double amount, double min, double max) {
            this.range = range; this.amount = amount; this.min = min; this.max = max;
        }
        public IOType getIOType() { return IOType.INPUT; }
        public int getChunkRange() { return range; }
        public double getAmount() { return amount; }
        public double getMinPerChunk() { return min; }
        public double getMaxPerChunk() { return max; }
    }
}
