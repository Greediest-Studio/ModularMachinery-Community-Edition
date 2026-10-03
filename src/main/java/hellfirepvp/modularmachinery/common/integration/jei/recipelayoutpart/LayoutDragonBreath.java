// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DragonBreath;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;
import hellfirepvp.modularmachinery.common.integration.jei.render.DragonBreathRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.awt.*;

public class LayoutDragonBreath extends BaseRecipeLayoutPart<DragonBreath> {

    public LayoutDragonBreath(Point offset) {
        super(offset, DragonBreath.class);
    }

    @Override
    public IIngredientRenderer<DragonBreath> provideIngredientRenderer() {
        return new DragonBreathRenderer();
    }
}
