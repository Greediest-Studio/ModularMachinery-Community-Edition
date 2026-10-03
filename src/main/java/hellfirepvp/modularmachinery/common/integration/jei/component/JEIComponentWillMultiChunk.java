// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementWillMultiChunk;
import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DemonWill;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutWill;

import java.awt.*;

public class JEIComponentWillMultiChunk extends JEIComponentBase<DemonWill> {

    public JEIComponentWillMultiChunk(RequirementWillMultiChunk requirement) {
        super(new DemonWill(requirement.willType, requirement.getAmount()), DemonWill.class);
    }

    @Override
    public RecipeLayoutPart<DemonWill> getLayoutPart(Point offset) {
        return new LayoutWill(offset);
    }

}
