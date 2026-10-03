package hellfirepvp.modularmachinery.common.integration.gregtech;

import hellfirepvp.modularmachinery.common.integration.gregtech.componentproxy.GTEnergyHatchProxy;
import hellfirepvp.modularmachinery.common.integration.gregtech.componentproxy.GTFluidHatchProxy;
import hellfirepvp.modularmachinery.common.integration.gregtech.componentproxy.GTItemBusProxy;
import hellfirepvp.modularmachinery.common.integration.gregtech.patternproxy.GTBlockMachineProxy;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentProxyRegistry;
import hellfirepvp.modularmachinery.common.machine.pattern.SpecialItemBlockProxyRegistry;

public class ModIntegrationGTCEU {

    public static void initialize() {
        MachineComponentProxyRegistry.INSTANCE.register("GTEnergyHatchProxy", GTEnergyHatchProxy.INSTANCE);
        MachineComponentProxyRegistry.INSTANCE.register("GTItemBusProxy", GTItemBusProxy.INSTANCE);
        MachineComponentProxyRegistry.INSTANCE.register("GTFluidHatchProxy", GTFluidHatchProxy.INSTANCE);

        SpecialItemBlockProxyRegistry.INSTANCE.register("GTBlockMachineProxy", GTBlockMachineProxy.INSTANCE);
    }

}
