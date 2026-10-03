// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.VisRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutVis extends BaseRecipeLayoutPart<Vis> {
    public LayoutVis(Point offset) {
        super(offset, Vis.class);
    }

    @Override
    public IIngredientRenderer<Vis> provideIngredientRenderer() {
        return new VisRenderer();
    }
}
