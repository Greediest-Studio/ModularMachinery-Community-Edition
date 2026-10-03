package hellfirepvp.modularmachinery.common.integration.crafttweaker.helper;

import com.github.bsideup.jabel.Desugar;
import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.upgrade.MachineUpgrade;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@Desugar
@ZenRegister
@ZenClass("mods.modularmachinery.UpgradeEventHandlerWrapper")
public record UpgradeEventHandlerWrapper(@ZenGetter("event") MachineEvent event,
                                         @ZenGetter("upgrade") MachineUpgrade upgrade) {
}
