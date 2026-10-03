package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementVis;
import net.minecraft.util.math.BlockPos;
import java.util.Map;

public class VisSnapshot extends NumericResourceSnapshot<RequirementVis> {
    public VisSnapshot(int range, BlockPos center, Map<Long, Float> amounts) {
        super(range, center, amounts);
    }
}
