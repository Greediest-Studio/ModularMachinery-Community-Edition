// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DimensionHelper extends BaseIngredientHelper<Dimension> {

    @Override
    public String getUniqueId(Dimension dimension) {
        return String.valueOf(dimension.getId());
    }

    @Override
    public String getResourceId(Dimension dimension) {
        return getUniqueId(dimension);
    }

    @Override
    public Dimension copyIngredient(Dimension dimension) {
        return new Dimension(dimension.getId(), dimension.getName());
    }
}
