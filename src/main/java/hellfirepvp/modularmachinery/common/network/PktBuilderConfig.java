package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBuilderTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public class PktBuilderConfig implements IMessage, IMessageHandler<PktBuilderConfig, IMessage> {
    private int window;
    private NBTTagCompound tag;
    public PktBuilderConfig() {}
    public PktBuilderConfig(int window, NBTTagCompound tag) { this.window = window; this.tag = tag; }
    @Override public void fromBytes(ByteBuf buf) { window = buf.readInt(); tag = ByteBufUtils.readTag(buf); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(window); ByteBufUtils.writeTag(buf, tag); }
    @Override public IMessage onMessage(PktBuilderConfig msg, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (msg.tag != null && player.openContainer instanceof ContainerBuilderTool c && c.windowId == msg.window && c.canInteractWith(player)) {
                c.applySettings(msg.tag);
                c.sync(player);
            }
        });
        return null;
    }
}
