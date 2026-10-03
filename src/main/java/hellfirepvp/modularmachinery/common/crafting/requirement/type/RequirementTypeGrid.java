package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGrid;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Grid;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

import javax.annotation.Nullable;

public class RequirementTypeGrid extends RequirementType<Grid, RequirementGrid> {

    @Override
    public ComponentRequirement<Grid, ? extends RequirementType<Grid, RequirementGrid>> createRequirement(IOType type, JsonObject json) {
        float power = RequirementUtils.getRequiredPositiveFloat(json, "power", ModularMagicRequirements.KEY_REQUIREMENT_GRID.toString());
        return new RequirementGrid(type, power);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "extrautils2";
    }
}
