// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.render.base;

import hellfirepvp.modularmachinery.common.integration.JeiPlugin;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.formatting.ITooltippable;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseIngredientRenderer<T extends ITooltippable> implements IIngredientRenderer<T> {
    protected IDrawableBuilder builder;

    @Override
    public void render(@Nonnull Minecraft minecraft, int x, int y, @Nullable T toRender) {
        if (toRender == null) {return;}

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (builder == null) {
            builder = JeiPlugin.GUI_HELPER.drawableBuilder(getTexture(toRender), 0, 0, 16, 16);
        }
        builder.setTextureSize(16, 16);
        builder.build().draw(minecraft, x, y);

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    @Nonnull
    public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull T ingredient, @Nonnull ITooltipFlag tooltipFlag) {
        return ingredient.getTooltip();
    }

    public abstract ResourceLocation getTexture(T ingredient);
}
