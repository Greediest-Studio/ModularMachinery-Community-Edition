// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.render;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import hellfirepvp.modularmachinery.common.integration.jei.render.base.BaseIngredientRenderer;
import net.minecraft.util.ResourceLocation;

public class BiomeRenderer extends BaseIngredientRenderer<Biome> {

    @Override
    public ResourceLocation getTexture(Biome ingredient) {
        return new ResourceLocation(ModularMachinery.MODID, "textures/gui/jei/jei_biome.png");
    }
}
