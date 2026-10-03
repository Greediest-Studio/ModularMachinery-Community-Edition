package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.distribution.ConstellationSkyHandler;
import hellfirepvp.astralsorcery.common.constellation.distribution.WorldSkyHandler;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentConstellationProvider;

import javax.annotation.Nullable;

public class TileConstellationProvider extends TileColorableMachineComponent implements MachineComponentTile {

    public boolean isConstellationInSky(IConstellation constellation) {
        WorldSkyHandler worldHandler = ConstellationSkyHandler.getInstance().getWorldHandler(world);
        return worldHandler != null && worldHandler.isActive(constellation);
    }

    @Nullable
    @Override
    public MachineComponentConstellationProvider provideComponent() {
        return new MachineComponentConstellationProvider(this, IOType.INPUT);
    }
}
