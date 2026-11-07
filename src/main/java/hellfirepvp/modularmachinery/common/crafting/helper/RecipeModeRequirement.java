package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ControllerMode;

/** 独立于脚本预检查的模式条件，不消耗输入，也不改变控制器模式。 */
public final class RecipeModeRequirement {
    private final String name;
    private final int value;

    public RecipeModeRequirement(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public CraftCheck check(TileMultiblockMachineController controller) {
        DynamicMachine machine = controller.getFoundMachine();
        if (machine == null || !controller.isStructureFormed()) {
            return CraftCheck.failure("gui.controller.status.missing_structure");
        }
        ControllerMode mode = machine.getControllerMode(name);
        if (mode == null || !mode.getModes().containsKey(value)) {
            return CraftCheck.failure("craftcheck.failure.controller.mode.undefined");
        }
        if (controller.getControllerMode(name) != value) {
            return CraftCheck.failure("craftcheck.failure.controller.mode.mismatch");
        }
        return CraftCheck.success();
    }
}
