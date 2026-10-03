// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RadiationHelper extends BaseIngredientHelper<Radiation> {

    @Override
    public String getUniqueId(Radiation t) {
        return "Radiation";
    }

    @Override
    public String getResourceId(Radiation t) {
        return getUniqueId(t);
    }

    @Override
    public Radiation copyIngredient(Radiation t) {
        return new Radiation(t.getAmount(), t.getChunkRange(), t.isScrubber());
    }
}
