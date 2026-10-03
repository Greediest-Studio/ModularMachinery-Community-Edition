package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.crafting.component.ModularMagicComponents;
import hellfirepvp.modularmachinery.common.crafting.helper.ImpetusProviderCopy;
import hellfirepvp.modularmachinery.common.tiles.TileImpetusComponent;

/**
 * @author youyihj
 */
public class MachineComponentImpetus extends MachineComponent<ImpetusProviderCopy> {
    private final ImpetusProviderCopy provider;

    public MachineComponentImpetus(IOType ioType, TileImpetusComponent provider) {
        super(ioType);
        this.provider = new ImpetusProviderCopy(provider);
    }

    @Override
    public ComponentType getComponentType() {
        return RegistriesMM.COMPONENT_TYPE_REGISTRY.getValue(ModularMagicComponents.KEY_COMPONENT_IMPETUS);
    }

    @Override
    public ImpetusProviderCopy getContainerProvider() {
        return provider;
    }
}
