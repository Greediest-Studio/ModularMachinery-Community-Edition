package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.crafting.component.ModularMagicComponents;
import hellfirepvp.modularmachinery.common.tiles.TileGridProvider;

public class MachineComponentGridProvider extends MachineComponent<TileGridProvider> {

    private final TileGridProvider gridProvider;

    public MachineComponentGridProvider(TileGridProvider gridProvider, IOType ioType) {
        super(ioType);

        this.gridProvider = gridProvider;
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ModularMagicComponents.KEY_COMPONENT_GRID);
    }

    @Override
    public TileGridProvider getContainerProvider() {
        return this.gridProvider;
    }
}
