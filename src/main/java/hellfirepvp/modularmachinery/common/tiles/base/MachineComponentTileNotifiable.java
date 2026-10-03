package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.event.machine.MachineEvent;

public interface MachineComponentTileNotifiable extends MachineComponentTile {

    void onMachineEvent(final MachineEvent event);

}
