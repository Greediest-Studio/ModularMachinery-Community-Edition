// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutBiome;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentBiome extends JEIComponentBase<Biome> {

    public JEIComponentBiome(Biome requirement) {
        super(requirement, Biome.class);
    }

    @Override
    public RecipeLayoutPart<Biome> getLayoutPart(Point offset) {
        return new LayoutBiome(offset);
    }
}
