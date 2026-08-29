/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;

import javax.annotation.Nonnull;

public class TileParallelEqualizerHatch extends TileColorableMachineComponent implements MachineComponentTile {

    private final MachineComponent<TileParallelEqualizerHatch> component =
        new MachineComponent<TileParallelEqualizerHatch>(IOType.INPUT) {
            @Override
            public ComponentType getComponentType() {
                return ComponentTypesMM.COMPONENT_PARALLEL_EQUALIZER;
            }

            @Override
            public TileParallelEqualizerHatch getContainerProvider() {
                return TileParallelEqualizerHatch.this;
            }
        };

    @Nonnull
    @Override
    public MachineComponent<TileParallelEqualizerHatch> provideComponent() {
        return component;
    }
}
