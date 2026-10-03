// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.BiomeRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutBiome extends BaseRecipeLayoutPart<Biome> {

    public LayoutBiome(Point offset) {
        super(offset, Biome.class);
    }

    @Override
    public IIngredientRenderer<Biome> provideIngredientRenderer() {
        return new BiomeRenderer();
    }
}
