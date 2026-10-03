// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementMeteor;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.BLOODMAGIC_ID)
public class RequirementTypeMeteor extends BaseRequirementType<Meteor, RequirementMeteor> {

    @Override
    public ComponentRequirement<Meteor, ? extends RequirementType<Meteor, RequirementMeteor>> createRequirement(IOType type, JsonObject jsonObject) {
        String itemString = RequirementUtils.getRequiredString(jsonObject, "item", RequirementMeteor.class.getSimpleName());

        return RequirementMeteor.from(itemString);
    }
}
