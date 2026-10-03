// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.event.machine;

import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;

public class MachineControllerInvalidatedEvent extends MachineEvent {

    public MachineControllerInvalidatedEvent(TileMultiblockMachineController controller) {
        super(controller);
    }
}
