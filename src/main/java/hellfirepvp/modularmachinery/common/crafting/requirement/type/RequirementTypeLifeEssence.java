package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementLifeEssence;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.LifeEssence;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeLifeEssence extends RequirementType<LifeEssence, RequirementLifeEssence> {

    @Override
    public ComponentRequirement<LifeEssence, ? extends RequirementType<LifeEssence, RequirementLifeEssence>> createRequirement(IOType type, JsonObject json) {
        int amount = RequirementUtils.getRequiredInt(json, "amount", ModularMagicRequirements.KEY_REQUIREMENT_LIFE_ESSENCE.toString());
        boolean perTick = RequirementUtils.getOptionalBoolean(json, "perTick", false);
        return new RequirementLifeEssence(type, amount, perTick);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "bloodmagic";
    }
}
