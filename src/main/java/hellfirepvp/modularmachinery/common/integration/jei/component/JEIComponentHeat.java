// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutHeat;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentHeat extends JEIComponentBase<Heat> {

    public JEIComponentHeat(Heat requirement) {
        super(requirement, Heat.class);
    }

    @Override
    public RecipeLayoutPart<Heat> getLayoutPart(Point offset) {
        return new LayoutHeat(offset);
    }
}
