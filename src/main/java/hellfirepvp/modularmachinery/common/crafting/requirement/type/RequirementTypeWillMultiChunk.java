// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementWillMultiChunk;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DemonWill;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.BLOODMAGIC_ID)
public class RequirementTypeWillMultiChunk extends BaseRequirementType<DemonWill, RequirementWillMultiChunk> {

    @Override
    public ComponentRequirement<DemonWill, ? extends RequirementType<DemonWill, RequirementWillMultiChunk>> createRequirement(IOType type, JsonObject json) {
        String willType = RequirementUtils.getRequiredString(json, "will-type", ModularMagicRequirements.KEY_REQUIREMENT_WILL.toString());
        double amount = RequirementUtils.getRequiredDouble(json, "amount", RequirementTypesMM.KEY_REQUIREMENT_WILL_MULTI_CHUNK.toString());
        double min = RequirementUtils.getOptionalDouble(json, "minPerChunk", 0.0F);
        double max = RequirementUtils.getOptionalDouble(json, "maxPerChunk", Integer.MAX_VALUE);
        int chunkRange = RequirementUtils.getOptionalInt(json, "chunkRange", 0); // Only the chunk the machine is in

        return RequirementWillMultiChunk.from(type, chunkRange, amount, min, max, willType);
    }
}
