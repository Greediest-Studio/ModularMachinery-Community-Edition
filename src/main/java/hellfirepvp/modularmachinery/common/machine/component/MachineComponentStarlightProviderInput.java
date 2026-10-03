package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.crafting.component.ModularMagicComponents;
import hellfirepvp.modularmachinery.common.tiles.TileStarlightInput;

public class MachineComponentStarlightProviderInput extends MachineComponent<TileStarlightInput> {

    private final TileStarlightInput starlightProvider;

    public MachineComponentStarlightProviderInput(TileStarlightInput starlightProvider, IOType ioType) {
        super(ioType);
        this.starlightProvider = starlightProvider;
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ModularMagicComponents.KEY_COMPONENT_STARLIGHT);
    }

    @Override
    public TileStarlightInput getContainerProvider() {
        return starlightProvider;
    }
}
