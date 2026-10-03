// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.event.machine;

import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;

/**
 * This event is fired when the machine controller is affected by redstone. This allows processing components
 * to react to the machine being stopped, if anything needs to happen.
 */
public class MachineControllerRedstoneAffectedEvent extends MachineEvent {

    // The status of the machine (true if it is powered, false otherwise)
    public boolean isPowered;

    public MachineControllerRedstoneAffectedEvent(TileMultiblockMachineController controller, boolean isPowered) {
        super(controller);
        this.isPowered = isPowered;
    }
}
