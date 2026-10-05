package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerBuilderTool;
import hellfirepvp.modularmachinery.common.machine.assembly.BuilderToolSettings;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;
import java.util.*;

public class PktBuilderData implements IMessage, IMessageHandler<PktBuilderData, IMessage> {
    private int window;
    private NBTTagCompound data;
    public PktBuilderData() {}
    public PktBuilderData(ContainerBuilderTool c) {
        window = c.windowId;
        data = new NBTTagCompound();
        data.setTag("options", BuilderToolSettings.write(c.options));
        data.setBoolean("ae", c.hasAe);
        data.setBoolean("fluidCrafting", c.hasFluidCrafting);
        data.setBoolean("complement", c.hasComplement);
        NBTTagCompound catalog = new NBTTagCompound();
        c.catalog.forEach((name, specs) -> {
            NBTTagList list = new NBTTagList();
            specs.forEach(spec -> list.appendTag(new NBTTagString(spec)));
            catalog.setTag(name, list);
        });
        data.setTag("catalog", catalog);
    }
    @Override public void fromBytes(ByteBuf buf) { window = buf.readInt(); data = ByteBufUtils.readTag(buf); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(window); ByteBufUtils.writeTag(buf, data); }
    @Override @SideOnly(Side.CLIENT)
    public IMessage onMessage(PktBuilderData msg, MessageContext ctx) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (msg.data == null || mc.player == null || !(mc.player.openContainer instanceof ContainerBuilderTool c) || c.windowId != msg.window) return;
            boolean initial = !c.ready;
            c.options = BuilderToolSettings.read(msg.data.getCompoundTag("options"));
            c.hasAe = msg.data.getBoolean("ae");
            c.hasFluidCrafting = msg.data.getBoolean("fluidCrafting");
            c.hasComplement = msg.data.getBoolean("complement");
            c.catalog.clear();
            NBTTagCompound catalog = msg.data.getCompoundTag("catalog");
            for (String name : catalog.getKeySet()) {
                NBTTagList list = catalog.getTagList(name, 8);
                List<String> specs = new ArrayList<>();
                for (int i = 0; i < list.tagCount(); i++) specs.add(list.getStringTagAt(i));
                c.catalog.put(name, specs);
            }
            c.ready = true;
            if (initial && mc.currentScreen instanceof hellfirepvp.modularmachinery.client.gui.GuiBuilderTool gui) gui.refresh();
        });
        return null;
    }
}
