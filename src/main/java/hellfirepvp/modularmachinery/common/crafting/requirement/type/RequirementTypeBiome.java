// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementBiome;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

public class RequirementTypeBiome extends BaseRequirementType<Biome, RequirementBiome> {

    @Override
    public ComponentRequirement<Biome, ? extends RequirementType<Biome, RequirementBiome>> createRequirement(IOType type, JsonObject jsonObject) {
        return RequirementBiome.from(type, RequirementUtils.getOptionalString(jsonObject, "biomeRegistryName"));
    }
}
