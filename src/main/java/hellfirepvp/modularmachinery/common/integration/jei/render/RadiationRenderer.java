// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.render;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.integration.jei.render.base.BaseIngredientRenderer;
import net.minecraft.util.ResourceLocation;

public class RadiationRenderer extends BaseIngredientRenderer<Radiation> {

    @Override
    public ResourceLocation getTexture(Radiation ingredient) {
        return new ResourceLocation(ModularMachinery.MODID, "textures/gui/jei/overlay_radiationprovider.png");
    }
}
