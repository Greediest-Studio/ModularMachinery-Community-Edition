package hellfirepvp.modularmachinery.common.integration.crafttweaker.helper;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import github.kasuminova.mmce.common.helper.IMachineController;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.modularmachinery.DynamicOutputAdder")
@FunctionalInterface
public interface DynamicOutputAdder {
    /**
     * 根据控制器状态和当前配方上下文，返回要额外添加的输出物品数组
     *
     * @param controller 机器控制器
     * @return 要添加的额外输出物品数组
     */
    IItemStack[] getAdditionalOutputs(IMachineController controller);
}