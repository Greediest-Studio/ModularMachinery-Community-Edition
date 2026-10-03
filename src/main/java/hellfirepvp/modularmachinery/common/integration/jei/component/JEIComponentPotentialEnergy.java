// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutPotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentPotentialEnergy extends JEIComponentBase<PotentialEnergy> {

    public JEIComponentPotentialEnergy(PotentialEnergy requirement) {
        super(requirement, PotentialEnergy.class);
    }

    @Override
    public RecipeLayoutPart<PotentialEnergy> getLayoutPart(Point point) {
        return new LayoutPotentialEnergy(point);
    }
}
