// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFlux;

import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.IOType;

public class MachineComponentFluxProvider extends BaseMachineComponent<RequirementFlux> {

    public MachineComponentFluxProvider(IRequirementHandler<RequirementFlux> fluxProvider, IOType ioType) {
        super(ioType, fluxProvider);
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ComponentTypesMM.KEY_COMPONENT_FLUX);
    }
}
