// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutMeteor;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentMeteor extends JEIComponentBase<Meteor> {

    public JEIComponentMeteor(Meteor meteor) {
        super(meteor, Meteor.class);
    }

    @Override
    public RecipeLayoutPart<Meteor> getLayoutPart(Point offset) {
        return new LayoutMeteor(offset);
    }
}
