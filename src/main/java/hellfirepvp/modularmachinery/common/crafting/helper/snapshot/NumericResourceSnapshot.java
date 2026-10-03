package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Vis、Flux 和辐射共用的不可变区块数值快照。 */
public class NumericResourceSnapshot<T extends IMultiChunkRequirement> extends AbstractSnapshot<T> {
    private final Map<Long, Double> amounts;

    public NumericResourceSnapshot(int range, BlockPos center, Map<Long, ? extends Number> amounts) {
        super(range, center.toImmutable());
        Map<Long, Double> copy = new HashMap<>();
        amounts.forEach((pos, amount) -> copy.put(pos, amount.doubleValue()));
        this.amounts = Collections.unmodifiableMap(copy);
    }

    @Override
    public ISnapshot<T> getSnapshotForRange(int range) {
        ChunkPos origin = new ChunkPos(center);
        Map<Long, Double> selected = new HashMap<>();
        amounts.forEach((pos, amount) -> {
            if (isChunkWithinRange(pos, origin, range)) selected.put(pos, amount);
        });
        return new NumericResourceSnapshot<>(Math.min(range, coveredRange), center, selected);
    }

    @Override
    public boolean canHandleInput(T requirement) {
        return hasCapacity(requirement, true);
    }

    @Override
    public boolean canHandleOutput(T requirement) {
        return hasCapacity(requirement, false);
    }

    private boolean hasCapacity(T requirement, boolean input) {
        if (requirement.getChunkRange() > coveredRange) return false;
        double remaining = requirement.getAmount();
        if (remaining <= 0) return true;
        ChunkPos origin = new ChunkPos(center);
        for (Map.Entry<Long, Double> entry : amounts.entrySet()) {
            if (!isChunkWithinRange(entry.getKey(), origin, requirement.getChunkRange())) continue;
            double stored = entry.getValue();
            remaining -= Math.max(0, input ? stored - requirement.getMinPerChunk()
                : requirement.getMaxPerChunk() - stored);
            if (remaining <= 0) return true;
        }
        return false;
    }
}
