// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Flux;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluxHelper extends BaseIngredientHelper<Flux> {

    @Override
    public String getUniqueId(Flux flux) {
        return getDisplayName(flux);
    }

    @Override
    public String getResourceId(Flux flux) {
        return getUniqueId(flux);
    }

    @Override
    public Flux copyIngredient(Flux flux) {
        return new Flux(flux.amount(), flux.chunkRange());
    }
}
