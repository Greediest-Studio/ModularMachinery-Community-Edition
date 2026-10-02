package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementCatalyst;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;

public class RequirementTypeCatalyst extends RequirementTypeIngredientArray {

    @Override
    public RequirementCatalyst createRequirement(IOType type, JsonObject jsonObject) {
        RequirementIngredientArray base = super.createRequirement(type, jsonObject);
        RequirementCatalyst catalyst = new RequirementCatalyst(base.getIngredients());
        catalyst.setChance(base.chance);

        if (jsonObject.has("tooltips") && jsonObject.get("tooltips").isJsonArray()) {
            JsonArray tooltips = jsonObject.getAsJsonArray("tooltips");
            for (JsonElement tip : tooltips) {
                if (tip.isJsonPrimitive()) {
                    catalyst.addTooltip(tip.getAsString());
                }
            }
        }

        if (jsonObject.has("modifiers") && jsonObject.get("modifiers").isJsonArray()) {
            JsonArray mods = jsonObject.getAsJsonArray("modifiers");
            RecipeModifier.Deserializer deserializer = new RecipeModifier.Deserializer();
            for (JsonElement modElement : mods) {
                if (modElement.isJsonObject()) {
                    RecipeModifier mod = deserializer.deserialize(modElement, RecipeModifier.class, null);
                    if (mod != null) {
                        catalyst.addModifier(mod);
                    }
                }
            }
        }

        if (jsonObject.has("consumePerParallel") && jsonObject.get("consumePerParallel").isJsonPrimitive()) {
            catalyst.setConsumePerParallel(jsonObject.getAsJsonPrimitive("consumePerParallel").getAsBoolean());
        }

        return catalyst;
    }
}

