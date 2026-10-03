package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFlux;
import net.minecraft.util.math.BlockPos;
import java.util.Map;

public class FluxSnapshot extends NumericResourceSnapshot<RequirementFlux> {
    public FluxSnapshot(int range, BlockPos center, Map<Long, Float> amounts) {
        super(range, center, amounts);
    }
}
