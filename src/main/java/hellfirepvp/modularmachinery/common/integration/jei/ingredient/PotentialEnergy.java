// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.ingredient;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.integration.jei.IRequiresEquals;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.formatting.FormatUtils;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.formatting.ITooltippable;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.List;

public class PotentialEnergy implements IRequiresEquals<PotentialEnergy>, IIngredientType<PotentialEnergy>, ITooltippable {

    private final float energy;

    public PotentialEnergy() {
        this(0.0f);
    }

    public PotentialEnergy(float energy) {
        this.energy = energy;
    }

    public float getEnergy() {
        return energy;
    }

    @Override
    @Nonnull
    public Class<? extends PotentialEnergy> getIngredientClass() {
        return this.getClass();
    }

    @Override
    public boolean equalsTo(PotentialEnergy other) {
        return this.energy == other.energy;
    }

    @Override
    public List<String> getTooltip() {
        return Lists.newArrayList(FormatUtils.format(I18n.format("jei.tooltip.modularmachinery.potential_energy"), String.valueOf(energy)));
    }
}
