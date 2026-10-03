// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutDimension;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentDimension extends JEIComponentBase<Dimension> {

    public JEIComponentDimension(Dimension requirement) {
        super(requirement, Dimension.class);
    }

    @Override
    public RecipeLayoutPart<Dimension> getLayoutPart(Point offset) {
        return new LayoutDimension(offset);
    }
}
