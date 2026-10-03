// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.DimensionRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutDimension extends BaseRecipeLayoutPart<Dimension> {

    public LayoutDimension(Point offset) {
        super(offset, Dimension.class);
    }

    @Override
    public IIngredientRenderer<Dimension> provideIngredientRenderer() {
        return new DimensionRenderer();
    }
}
