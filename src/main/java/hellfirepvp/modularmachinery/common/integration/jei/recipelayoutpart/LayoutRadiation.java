// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.RadiationRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutRadiation extends BaseRecipeLayoutPart<Radiation> {

    public LayoutRadiation(Point offset) {
        super(offset, Radiation.class);
    }

    @Override
    public IIngredientRenderer<Radiation> provideIngredientRenderer() {
        return new RadiationRenderer();
    }
}
