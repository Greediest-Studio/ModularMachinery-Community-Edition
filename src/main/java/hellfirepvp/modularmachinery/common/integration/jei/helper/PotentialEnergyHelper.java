// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PotentialEnergyHelper extends BaseIngredientHelper<PotentialEnergy> {

    @Override
    public String getUniqueId(PotentialEnergy potentialEnergy) {
        return getDisplayName(potentialEnergy);
    }

    @Override
    public String getResourceId(PotentialEnergy potentialEnergy) {
        return getUniqueId(potentialEnergy);
    }

    @Override
    public PotentialEnergy copyIngredient(PotentialEnergy potentialEnergy) {
        return new PotentialEnergy(potentialEnergy.getEnergy());
    }
}
