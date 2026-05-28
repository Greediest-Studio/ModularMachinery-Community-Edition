package github.kasuminova.mmce.common.event.machine;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineUpgradeChangeEvent")
public class MachineUpgradeChangeEvent extends MachineEvent {
    public MachineUpgradeChangeEvent(TileMultiblockMachineController controller) {
        super(controller);
    }
}