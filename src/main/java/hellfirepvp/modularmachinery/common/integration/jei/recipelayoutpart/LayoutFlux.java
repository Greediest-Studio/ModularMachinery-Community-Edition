// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Flux;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.FluxRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutFlux extends BaseRecipeLayoutPart<Flux> {
    public LayoutFlux(Point offset, Class<Flux> clazz) {
        super(offset, clazz);
    }

    @Override
    public IIngredientRenderer<Flux> provideIngredientRenderer() {
        return new FluxRenderer();
    }
}
