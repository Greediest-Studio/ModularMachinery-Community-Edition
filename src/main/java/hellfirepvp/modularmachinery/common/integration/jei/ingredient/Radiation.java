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

public class Radiation implements IRequiresEquals<Radiation>, IIngredientType<Radiation>, ITooltippable {
    private double amount;
    private int chunkRange;
    private boolean scrubber; // I know, this sucks. But you know what else sucks? More boilerplate code hehe

    public Radiation() {
    }

    public Radiation(double amount, int chunkRange, boolean scrubber) {
        this.amount = amount;
        this.chunkRange = chunkRange;
        this.scrubber = scrubber;
    }

    public double getAmount() {
        return amount;
    }

    public int getChunkRange() {
        return chunkRange;
    }

    public boolean isScrubber() {
        return scrubber;
    }

    @Override
    public boolean equalsTo(Radiation other) {
        return amount == other.amount && chunkRange == other.chunkRange;
    }

    @Override
    @Nonnull
    public Class<? extends Radiation> getIngredientClass() {
        return this.getClass();
    }

    @Override
    public List<String> getTooltip() {
        List<String> tooltip = Lists.newArrayList();
        tooltip.add(FormatUtils.format(I18n.format(LocalizationKeys.RADIATION), scrubber ? I18n.format(LocalizationKeys.SCRUBBER_SPECIAL) : String.valueOf(this.amount)));
        tooltip.add(FormatUtils.format(I18n.format(LocalizationKeys.CHUNK_RANGE), String.valueOf(this.chunkRange)));
        return tooltip;
    }
}
