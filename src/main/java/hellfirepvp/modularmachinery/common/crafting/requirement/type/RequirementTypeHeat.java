// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementHeat;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.MEKANISM_ID)
public class RequirementTypeHeat extends BaseRequirementType<Heat, RequirementHeat> {

    @Override
    public ComponentRequirement<Heat, ? extends RequirementType<Heat, RequirementHeat>> createRequirement(IOType type, JsonObject jsonObject) {
        return RequirementHeat.from(type, RequirementUtils.getRequiredDouble(jsonObject, "amount", "heat"));
    }
}
