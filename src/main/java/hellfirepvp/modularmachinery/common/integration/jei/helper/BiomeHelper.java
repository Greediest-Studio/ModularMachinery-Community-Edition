// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BiomeHelper extends BaseIngredientHelper<Biome> {

    @Override
    public String getUniqueId(Biome biome) {
        return biome.getRegistryName();
    }

    @Override
    public String getResourceId(Biome biome) {
        return getUniqueId(biome);
    }

    @Override
    public Biome copyIngredient(Biome biome) {
        return new Biome(biome.getRegistryName(), biome.getName());
    }
}
