// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeatHelper extends BaseIngredientHelper<Heat> {

    @Override
    public String getUniqueId(Heat heat) {
        return getDisplayName(heat);
    }

    @Override
    public String getResourceId(Heat heat) {
        return getUniqueId(heat);
    }

    @Override
    public Heat copyIngredient(Heat heat) {
        return new Heat(heat.temperature());
    }
}
