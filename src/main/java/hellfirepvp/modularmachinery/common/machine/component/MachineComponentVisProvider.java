// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementVis;

import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.IOType;

public class MachineComponentVisProvider extends BaseMachineComponent<RequirementVis> {

    public MachineComponentVisProvider(IOType ioType, IRequirementHandler<RequirementVis> visHandler) {
        super(ioType, visHandler);
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ComponentTypesMM.KEY_COMPONENT_VIS);
    }
}
