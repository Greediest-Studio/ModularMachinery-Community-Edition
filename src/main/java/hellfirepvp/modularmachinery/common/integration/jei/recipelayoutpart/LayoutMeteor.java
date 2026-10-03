// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.MeteorRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutMeteor extends BaseRecipeLayoutPart<Meteor> {

    public LayoutMeteor(Point offset) {
        super(offset, Meteor.class);
    }

    @Override
    public IIngredientRenderer<Meteor> provideIngredientRenderer() {
        return new MeteorRenderer();
    }
}
