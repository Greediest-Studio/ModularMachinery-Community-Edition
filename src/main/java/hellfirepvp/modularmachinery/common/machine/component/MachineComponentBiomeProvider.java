// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementBiome;

import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.IOType;

public class MachineComponentBiomeProvider extends BaseMachineComponent<RequirementBiome> {

    public MachineComponentBiomeProvider(IOType ioType, IRequirementHandler<RequirementBiome> biomeHandler) {
        super(ioType, biomeHandler);
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ComponentTypesMM.KEY_COMPONENT_BIOME);
    }
}
