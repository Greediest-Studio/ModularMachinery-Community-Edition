package hellfirepvp.modularmachinery.client.gui.widget.impl.patternprovider;

import hellfirepvp.modularmachinery.client.gui.GuiMEPatternProvider;
import hellfirepvp.modularmachinery.client.gui.util.TextureProperties;
import hellfirepvp.modularmachinery.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.client.gui.widget.container.Row;
import hellfirepvp.modularmachinery.client.gui.widget.impl.preview.IngredientList;
import hellfirepvp.modularmachinery.client.gui.widget.slot.SlotFluidVirtual;
import hellfirepvp.modularmachinery.client.gui.widget.slot.SlotGasVirtual;
import hellfirepvp.modularmachinery.client.gui.widget.slot.SlotItemVirtual;
import hellfirepvp.modularmachinery.client.gui.widget.slot.SlotVirtual;
import hellfirepvp.modularmachinery.common.base.Mods;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import java.util.List;
import java.util.function.Function;

public class PatternProviderIngredientList extends IngredientList {

    protected static SlotVirtual initSlot(final SlotVirtual slot) {
        return slot.setSlotTex(TextureProperties.of(GuiMEPatternProvider.GUI_TEXTURE, 220, 232));
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        scrollbar.setMargin(5, 1, 1, 1);
        scrollbar.setWidthHeight(9, 124);
        scrollbar.getScroll()
                 .setMouseDownTexture(198, 232)
                 .setHoveredTexture(187, 232)
                 .setTexture(176, 232)
                 .setUnavailableTexture(209, 232)
                 .setTextureLocation(GuiMEPatternProvider.GUI_TEXTURE)
                 .setWidthHeight(9, 18);
    }

    public PatternProviderIngredientList setStackList(final List<ItemStack> list, final List<FluidStack> fluidList, final List<?> gasList) {
        getWidgets().clear();

        Row row = new Row();
        int[] stackPerRow = {0};
        int totalSize = list.size() + fluidList.size() + gasList.size();

        row = addSlots(list, row, stackPerRow, totalSize, SlotItemVirtual::ofJEI);
        row = addSlots(fluidList, row, stackPerRow, totalSize, SlotFluidVirtual::ofJEI);
        if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
            row = addGasSlots(gasList, row, stackPerRow, totalSize);
        }

        addWidget(row.setUseScissor(false));
        return this;
    }

    protected <T> Row addSlots(final List<T> gasList, Row row, int[] stackPerRow, final int totalSize, final Function<T, SlotVirtual> slotSupplier) {
        for (int i = 0; i < gasList.size(); i++) {
            final T stack = gasList.get(i);
            row.addWidget(initSlot(slotSupplier.apply(stack)));
            stackPerRow[0]++;
            if (stackPerRow[0] >= maxStackPerRow && i + 1 < totalSize) {
                addWidget(row.setUseScissor(false));
                row = new Row();
                stackPerRow[0] = 0;
            }
        }
        return row;
    }

    @SuppressWarnings("unchecked")
    @Optional.Method(modid = "mekeng")
    private Row addGasSlots(final List<?> gasList, Row row, int[] stackPerRow, final int totalSize) {
        return addSlots((List<GasStack>) gasList, row, stackPerRow, totalSize, SlotGasVirtual::ofJEI);
    }

}
