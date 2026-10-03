// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component;

import hellfirepvp.modularmachinery.common.integration.jei.component.base.JEIComponentBase;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Flux;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.LayoutFlux;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;

import java.awt.*;

public class JEIComponentFlux extends JEIComponentBase<Flux> {

    public JEIComponentFlux(Flux requirement) {
        super(requirement, Flux.class);
    }

    @Override
    public RecipeLayoutPart<Flux> getLayoutPart(Point offset) {
        return new LayoutFlux(offset, Flux.class);
    }
}
