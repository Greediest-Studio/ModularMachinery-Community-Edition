// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DragonBreath;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutDragonBreath;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentDragonBreath extends JEIComponentBase<DragonBreath> {

    public JEIComponentDragonBreath(DragonBreath requirement) {
        super(requirement, DragonBreath.class);
    }

    @Override
    public RecipeLayoutPart<DragonBreath> getLayoutPart(Point point) {
        return new LayoutDragonBreath(point);
    }
}
