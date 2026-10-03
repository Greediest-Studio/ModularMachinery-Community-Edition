// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutLaser;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentLaser extends JEIComponentBase<Laser> {

    public JEIComponentLaser(Laser requirement) {
        super(requirement, Laser.class);
    }

    @Override
    public RecipeLayoutPart<Laser> getLayoutPart(Point offset) {
        return new LayoutLaser(offset);
    }
}
