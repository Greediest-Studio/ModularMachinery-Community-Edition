package hellfirepvp.modularmachinery.common.integration.crafttweaker.helper;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.upgrade.MachineUpgrade;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.modularmachinery.UpgradeEventHandler")
public interface UpgradeEventHandlerCT {
    void handle(MachineEvent event, MachineUpgrade upgrade);
}
