package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementMana;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Mana;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeMana extends RequirementType<Mana, RequirementMana> {

    @Override
    public ComponentRequirement<Mana, ? extends RequirementType<Mana, RequirementMana>> createRequirement(IOType type, JsonObject json) {
        int amount = RequirementUtils.getRequiredInt(json, "amount", ModularMagicRequirements.KEY_REQUIREMENT_MANA.toString());
        boolean perTick = RequirementUtils.getOptionalBoolean(json, "perTick", false);
        return new RequirementMana(type, amount, perTick);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "botania";
    }
}
