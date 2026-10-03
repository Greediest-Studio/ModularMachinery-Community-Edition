package hellfirepvp.modularmachinery.common.network;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import hellfirepvp.modularmachinery.common.container.ContainerMEItemOutputBusStackSize;
import hellfirepvp.modularmachinery.common.tiles.MEItemOutputBus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktMEOutputBusStackSizeChange implements IMessage, IMessageHandler<PktMEOutputBusStackSizeChange, IMessage> {
    private BlockPos pos = BlockPos.ORIGIN;
    private int stackSize = Integer.MAX_VALUE;

    public PktMEOutputBusStackSizeChange() {
    }

    public PktMEOutputBusStackSizeChange(final BlockPos pos, final int stackSize) {
        this.pos = pos;
        this.stackSize = stackSize;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.stackSize = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.stackSize);
    }

    @Override
    public IMessage onMessage(final PktMEOutputBusStackSizeChange message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {
            if (!(player.openContainer instanceof ContainerMEItemOutputBusStackSize container)) {
                return;
            }
            MEItemOutputBus outputBus = container.getOwner();
            if (outputBus.isInvalid() || outputBus.getWorld() != player.world
                || !outputBus.getPos().equals(message.pos)
                || player.getDistanceSq(message.pos) > 64
                || player.world.getTileEntity(message.pos) != outputBus
                || !container.canInteractWith(player)) {
                return;
            }
            // Security remains effective while the network is unpowered.
            IGridNode node = outputBus.getProxy().getNode();
            if (node == null) {
                return;
            }
            ISecurityGrid security = node.getGrid().getCache(ISecurityGrid.class);
            if (!security.hasPermission(player, SecurityPermissions.BUILD)) {
                return;
            }

            int validatedStackSize = Math.max(1, message.stackSize);

            outputBus.setConfiguredStackSize(validatedStackSize);
            container.stackSize = validatedStackSize;

            outputBus.markDirty();
        });

        return null;
    }
}
