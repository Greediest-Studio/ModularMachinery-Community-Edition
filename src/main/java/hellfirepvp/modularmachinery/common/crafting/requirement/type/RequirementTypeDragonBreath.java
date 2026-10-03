// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementDragonBreath;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DragonBreath;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.ICE_AND_FIRE_ID)
public class RequirementTypeDragonBreath extends BaseRequirementType<DragonBreath, RequirementDragonBreath> {

    @Override
    public ComponentRequirement<DragonBreath, RequirementTypeDragonBreath> createRequirement(IOType ioType, JsonObject jsonObject) {
        return RequirementDragonBreath.from(ioType, RequirementUtils.getOptionalString(jsonObject, "dragon-type"), RequirementUtils.getOptionalInt(jsonObject, "amount", -1));
    }

}
