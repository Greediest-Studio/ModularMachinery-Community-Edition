// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.render;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.jei.render.base.BaseIngredientRenderer;
import net.minecraft.util.ResourceLocation;

public class PotentialEnergyRenderer extends BaseIngredientRenderer<PotentialEnergy> {

    @Override
    public ResourceLocation getTexture(PotentialEnergy ingredient) {
        return new ResourceLocation(Mods.ABYSSALCRAFT_ID, "textures/items/necronomicon.png");
    }
}
