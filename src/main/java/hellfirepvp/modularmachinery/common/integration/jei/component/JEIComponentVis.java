// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutVis;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentVis extends JEIComponentBase<Vis> {
    public JEIComponentVis(Vis requirement) {
        super(requirement, Vis.class);
    }

    @Override
    public RecipeLayoutPart<Vis> getLayoutPart(Point offset) {
        return new LayoutVis(offset);
    }
}
