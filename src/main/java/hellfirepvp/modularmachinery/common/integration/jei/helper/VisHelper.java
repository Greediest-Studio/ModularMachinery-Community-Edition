// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VisHelper extends BaseIngredientHelper<Vis> {

    @Override
    public String getUniqueId(Vis vis) {
        return "Vis";
    }

    @Override
    public String getResourceId(Vis vis) {
        return getUniqueId(vis);
    }

    @Override
    public Vis copyIngredient(Vis vis) {
        return new Vis(vis.getAmount(), vis.getChunkRange(), vis.getMinPerChunk(), vis.getMaxPerChunk());
    }
}
