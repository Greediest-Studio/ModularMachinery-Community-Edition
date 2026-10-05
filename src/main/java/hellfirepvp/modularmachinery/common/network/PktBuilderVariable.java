package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBuilderTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public class PktBuilderVariable implements IMessage, IMessageHandler<PktBuilderVariable, IMessage> {
    private int window;
    private String alias, spec;
    public PktBuilderVariable() {}
    public PktBuilderVariable(int window, String alias, String spec) { this.window = window; this.alias = alias; this.spec = spec; }
    @Override public void fromBytes(ByteBuf buf) { window = buf.readInt(); alias = ByteBufUtils.readUTF8String(buf); spec = ByteBufUtils.readUTF8String(buf); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(window); ByteBufUtils.writeUTF8String(buf, alias); ByteBufUtils.writeUTF8String(buf, spec); }
    @Override public IMessage onMessage(PktBuilderVariable msg, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (player.openContainer instanceof ContainerBuilderTool c && c.windowId == msg.window && c.canInteractWith(player)) {
                c.select(msg.alias, msg.spec);
                c.sync(player);
            }
        });
        return null;
    }
}
