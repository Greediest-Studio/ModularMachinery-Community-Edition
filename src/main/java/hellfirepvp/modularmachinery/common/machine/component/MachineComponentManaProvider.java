package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.crafting.component.ModularMagicComponents;
import hellfirepvp.modularmachinery.common.crafting.helper.ManaProviderCopy;
import hellfirepvp.modularmachinery.common.tiles.TileManaProvider;

public class MachineComponentManaProvider extends MachineComponent<ManaProviderCopy> {

    private final ManaProviderCopy manaProvider;

    public MachineComponentManaProvider(IOType io, TileManaProvider manaProvider) {
        super(io);
        this.manaProvider = new ManaProviderCopy(manaProvider);
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ModularMagicComponents.KEY_COMPONENT_MANA);
    }

    @Override
    public ManaProviderCopy getContainerProvider() {
        return manaProvider;
    }

}
