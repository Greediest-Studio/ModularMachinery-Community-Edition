// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.PotentialEnergyRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutPotentialEnergy extends BaseRecipeLayoutPart<PotentialEnergy> {

    public LayoutPotentialEnergy(Point offset) {
        super(offset, PotentialEnergy.class);
    }

    @Override
    public IIngredientRenderer<PotentialEnergy> provideIngredientRenderer() {
        return new PotentialEnergyRenderer();
    }
}
