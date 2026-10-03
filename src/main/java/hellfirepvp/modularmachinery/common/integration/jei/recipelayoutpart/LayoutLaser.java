// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart;

import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import hellfirepvp.modularmachinery.common.integration.jei.recipelayoutpart.base.BaseRecipeLayoutPart;

import java.awt.*;

public class LayoutLaser extends BaseRecipeLayoutPart<Laser> {

    public LayoutLaser(Point offset) {
        super(offset, Laser.class);
    }
}
