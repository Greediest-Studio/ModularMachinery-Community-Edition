package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementStarlight;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Starlight;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeStarlight extends RequirementType<Starlight, RequirementStarlight> {

    @Override
    public ComponentRequirement<Starlight, ? extends RequirementType<Starlight, RequirementStarlight>> createRequirement(IOType type, JsonObject json) {
        float amount = RequirementUtils.getRequiredFloat(json, "amount", ModularMagicRequirements.KEY_REQUIREMENT_STARLIGHT.toString());
        return new RequirementStarlight(type, amount);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "astralsorcery";
    }
}
