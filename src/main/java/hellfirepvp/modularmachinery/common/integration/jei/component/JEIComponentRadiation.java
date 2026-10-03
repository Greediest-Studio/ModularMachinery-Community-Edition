// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.nuclearcraft.IRequirementRadiation;
import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutRadiation;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentRadiation extends JEIComponentBase<Radiation> {

    public JEIComponentRadiation(IRequirementRadiation requirementRadiation, boolean scrubber) {
        super(new Radiation(requirementRadiation.getAmount(), requirementRadiation.getChunkRange(), scrubber), Radiation.class);
    }

    @Override
    public RecipeLayoutPart<Radiation> getLayoutPart(Point point) {
        return new LayoutRadiation(point);
    }

}
