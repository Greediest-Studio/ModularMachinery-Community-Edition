// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.helper;

import hellfirepvp.modularmachinery.common.integration.jei.helper.base.BaseIngredientHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MeteorHelper extends BaseIngredientHelper<Meteor> {

    @Override
    public String getUniqueId(Meteor meteor) {
        return "Meteor";
    }

    @Override
    public String getResourceId(Meteor meteor) {
        return getUniqueId(meteor);
    }

    @Override
    public Meteor copyIngredient(Meteor meteor) {
        // Yes, I know, the components and so on...
        return new Meteor(meteor.getCatalystStack(), meteor.getComponents(), meteor.getExplosionStrength(), meteor.getRadius());
    }
}
