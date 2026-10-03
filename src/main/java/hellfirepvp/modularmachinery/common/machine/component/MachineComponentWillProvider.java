package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.crafting.component.ModularMagicComponents;
import hellfirepvp.modularmachinery.common.tiles.TileWillProvider;

public class MachineComponentWillProvider extends MachineComponent<TileWillProvider> {

    private final TileWillProvider willProvider;

    public MachineComponentWillProvider(TileWillProvider willProvider, IOType ioType) {
        super(ioType);
        this.willProvider = willProvider;
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ModularMagicComponents.KEY_COMPONENT_WILL);
    }

    @Override
    public TileWillProvider getContainerProvider() {
        return willProvider;
    }
}
