// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LaserHelper extends BaseIngredientHelper<Laser>  {

    @Override
    public String getUniqueId(Laser laser) {
        return getDisplayName(laser);
    }

    @Override
    public String getResourceId(Laser laser) {
        return getUniqueId(laser);
    }

    @Override
    public Laser copyIngredient(Laser laser) {
        return new Laser(laser.power());
    }
}
