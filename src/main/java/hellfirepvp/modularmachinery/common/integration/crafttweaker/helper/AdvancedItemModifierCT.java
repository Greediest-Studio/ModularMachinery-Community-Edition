package hellfirepvp.modularmachinery.common.integration.crafttweaker.helper;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.common.helper.IMachineController;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.modularmachinery.AdvancedItemModifier")
@FunctionalInterface
public interface AdvancedItemModifierCT {
    IItemStack apply(IMachineController controller, IItemStack stack);
}
