package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementConstellation;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Constellation;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeConstellation extends RequirementType<Constellation, RequirementConstellation> {

    @Override
    public ComponentRequirement<Constellation, ? extends RequirementType<Constellation, RequirementConstellation>> createRequirement(IOType type, JsonObject json) {
        IConstellation constellation = RequirementUtils.getConstellation(json, "constellation", ModularMagicRequirements.KEY_REQUIREMENT_CONSTELLATION.toString());
        return new RequirementConstellation(type, constellation);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "astralsorcery";
    }
}
