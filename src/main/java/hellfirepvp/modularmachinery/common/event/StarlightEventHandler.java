package hellfirepvp.modularmachinery.common.event;

import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import hellfirepvp.modularmachinery.common.tiles.TileStarlightInput;
import hellfirepvp.modularmachinery.common.tiles.TileStarlightOutput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StarlightEventHandler {

    @SubscribeEvent
    public static void onStarlightTransmissionRegister(StarlightNetworkEvent.TransmissionRegister event) {
        event.getRegistry().registerProvider(new TileStarlightInput.StarlightProviderReceiverProvider());
    }

    @SubscribeEvent
    public static void onStarlightSourceRegister(StarlightNetworkEvent.SourceProviderRegistry event) {
        event.getRegistry().registerProvider(new TileStarlightOutput.Provider());
    }
}
