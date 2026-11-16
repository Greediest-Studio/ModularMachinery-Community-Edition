package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktControllerGeoModelToggle implements IMessage, IMessageHandler<PktControllerGeoModelToggle, IMessage> {
    private int windowId;
    private boolean enabled;

    public PktControllerGeoModelToggle() {
    }

    public PktControllerGeoModelToggle(int windowId, boolean enabled) {
        this.windowId = windowId;
        this.enabled = enabled;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        enabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeBoolean(enabled);
    }

    @Override
    public IMessage onMessage(PktControllerGeoModelToggle message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (!(player.openContainer instanceof ContainerBase<?> container)
                || container.windowId != message.windowId
                || !(container.getOwner() instanceof TileMultiblockMachineController controller)) {
                return;
            }
            if (controller.isInvalid() || controller.getWorld() != player.world
                || controller.getWorld().getTileEntity(controller.getPos()) != controller
                || player.getDistanceSq(controller.getPos()) > 64) {
                return;
            }
            controller.setGeoModelEnabled(message.enabled);
        });
        return null;
    }
}
