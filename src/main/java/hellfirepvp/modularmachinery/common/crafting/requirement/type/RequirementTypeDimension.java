// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementDimension;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

public class RequirementTypeDimension extends BaseRequirementType<Dimension, RequirementDimension> {

    @Override
    public ComponentRequirement<Dimension, ? extends RequirementType<Dimension, RequirementDimension>> createRequirement(IOType type, JsonObject jsonObject) {
        return RequirementDimension.from(type, RequirementUtils.getRequiredInt(jsonObject, "id", "id"));
    }
}
