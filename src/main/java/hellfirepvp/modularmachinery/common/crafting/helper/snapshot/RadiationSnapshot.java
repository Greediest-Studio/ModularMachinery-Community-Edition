package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementRadiation;
import net.minecraft.util.math.BlockPos;
import java.util.Map;

public class RadiationSnapshot extends NumericResourceSnapshot<RequirementRadiation> {
    public RadiationSnapshot(int range, BlockPos center, Map<Long, Double> amounts) {
        super(range, center, amounts);
    }
}
