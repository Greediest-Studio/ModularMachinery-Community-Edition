package hellfirepvp.modularmachinery.common.integration.jei;

import hellfirepvp.modularmachinery.client.gui.GuiBuilderTool;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import java.awt.Rectangle;
import java.util.*;

public final class BuilderGhostIngredientHandler implements IGhostIngredientHandler<GuiBuilderTool> {
    @Override public <I> List<Target<I>> getTargets(GuiBuilderTool gui, I ingredient, boolean doStart) {
        if (!gui.showsGhostSlot() || !(ingredient instanceof ItemStack stack) || !(stack.getItem() instanceof ItemBlock)) return Collections.emptyList();
        return Collections.singletonList(new Target<I>() {
            public Rectangle getArea() { return gui.ghostArea(); }
            public void accept(I value) { if (value instanceof ItemStack chosen) gui.acceptGhost(chosen); }
        });
    }
    @Override public void onComplete() {}
}
