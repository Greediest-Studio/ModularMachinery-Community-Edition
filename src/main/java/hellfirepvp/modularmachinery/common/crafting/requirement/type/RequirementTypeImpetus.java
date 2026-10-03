package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementImpetus;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Impetus;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeImpetus extends RequirementType<Impetus, RequirementImpetus> {

    public static final RequirementTypeImpetus INSTANCE = new RequirementTypeImpetus();


    @Override
    public ComponentRequirement<Impetus, ? extends RequirementType<Impetus, RequirementImpetus>> createRequirement(IOType type, JsonObject json) {
        int amount = RequirementUtils.getRequiredInt(json, "amount", ModularMagicRequirements.KEY_REQUIREMENT_IMPETUS.toString());
        return new RequirementImpetus(type, amount);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "thaumicaugmentation";
    }
}