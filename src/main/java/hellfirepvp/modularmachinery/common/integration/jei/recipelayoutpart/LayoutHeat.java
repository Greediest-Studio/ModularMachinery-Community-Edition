// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;

import java.awt.*;

public class LayoutHeat extends BaseRecipeLayoutPart<Heat> {

    public LayoutHeat(Point offset) {
        super(offset, Heat.class);
    }
}
