package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ControllerMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktControllerModeSelect implements IMessage, IMessageHandler<PktControllerModeSelect, IMessage> {
    private int windowId;
    private String name;
    private int value;

    public PktControllerModeSelect() {
    }

    public PktControllerModeSelect(int windowId, String name, int value) {
        this.windowId = windowId;
        this.name = name;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(value);
    }

    @Override
    public IMessage onMessage(PktControllerModeSelect message, MessageContext ctx) {
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
            DynamicMachine machine = controller.getFoundMachine();
            ControllerMode mode = machine == null ? null : machine.getControllerMode(message.name);
            if (mode != null && mode.isControllerButtonVisible()) {
                controller.setControllerMode(message.name, message.value);
            }
        });
        return null;
    }
}
