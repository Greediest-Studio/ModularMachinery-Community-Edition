// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementPotentialEnergy;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.ABYSSALCRAFT_ID)
public class RequirementTypePotentialEnergy extends BaseRequirementType<PotentialEnergy, RequirementPotentialEnergy> {

    @Override
    public ComponentRequirement<PotentialEnergy, ? extends RequirementType<PotentialEnergy, RequirementPotentialEnergy>> createRequirement(IOType ioType, JsonObject jsonObject) {
        return RequirementPotentialEnergy.from(ioType, RequirementUtils.getRequiredPositiveFloat(jsonObject, "amount", "potential_energy"));
    }

}
