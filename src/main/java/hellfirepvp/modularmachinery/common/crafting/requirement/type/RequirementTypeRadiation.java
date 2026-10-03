// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementRadiation;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.NUCLEARCRAFT_ID)
public class RequirementTypeRadiation extends BaseRequirementType<Radiation, RequirementRadiation> {

    @Override
    public ComponentRequirement<Radiation, ? extends RequirementType<Radiation, RequirementRadiation>> createRequirement(IOType type, JsonObject jsonObject) {
        double amount = RequirementUtils.getRequiredDouble(jsonObject, "amount", RequirementTypesMM.KEY_REQUIREMENT_RADIATION.toString());
        int chunkRange = RequirementUtils.getOptionalInt(jsonObject, "chunkRange", 0); // Only the chunk the machine is in

        return RequirementRadiation.from(type, chunkRange, amount);
    }
}
