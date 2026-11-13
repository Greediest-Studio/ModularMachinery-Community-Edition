package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktControllerStructureRefresh implements IMessage, IMessageHandler<PktControllerStructureRefresh, IMessage> {
    private int windowId;

    public PktControllerStructureRefresh() {
    }

    public PktControllerStructureRefresh(int windowId) {
        this.windowId = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
    }

    @Override
    public IMessage onMessage(PktControllerStructureRefresh message, MessageContext ctx) {
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
            controller.refreshStructure();
        });
        return null;
    }
}
