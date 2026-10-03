// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementLaser;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.MEKANISM_ID)
public class RequirementTypeLaser extends BaseRequirementType<Laser, RequirementLaser> {
    @Override
    public ComponentRequirement<Laser, ? extends RequirementType<Laser, RequirementLaser>> createRequirement(IOType type, JsonObject jsonObject) {
        if (type == IOType.OUTPUT) {throw new RequirementPrerequisiteFailedException("output requirement type is not supported");}

        return RequirementLaser.from(IOType.INPUT, RequirementUtils.getRequiredPositiveDouble(jsonObject, "power", "laser"));
    }
}
