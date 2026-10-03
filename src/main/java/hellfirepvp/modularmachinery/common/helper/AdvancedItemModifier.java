package hellfirepvp.modularmachinery.common.helper;

import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface AdvancedItemModifier {
    ItemStack apply(TileMultiblockMachineController controller, ItemStack stack);
}
