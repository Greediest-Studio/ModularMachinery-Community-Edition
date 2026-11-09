package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 显示流体、气体在方块纹理图集中的图标。 */
public class ElementOutputIcon implements IElement {
    public static int ID;

    private static final int ICON_SIZE = 16;
    private static final int PADDING = 2;
    private static final int LABEL_SPACING = 4;

    private final ResourceLocation texture;
    private final int color;

    public ElementOutputIcon(final ResourceLocation texture, final int color) {
        this.texture = texture;
        this.color = color;
    }

    public ElementOutputIcon(final ByteBuf buf) {
        this(new ResourceLocation(ByteBufUtils.readUTF8String(buf)), buf.readInt());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final int x, final int y) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(texture.toString());
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
        RenderHelper.drawTexturedModalRect(x + PADDING, y + PADDING, sprite, ICON_SIZE, ICON_SIZE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
    }

    @Override
    public int getWidth() {
        return ICON_SIZE + PADDING * 2 + LABEL_SPACING;
    }

    @Override
    public int getHeight() {
        return ICON_SIZE + PADDING * 2;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, texture.toString());
        buf.writeInt(color);
    }

    @Override
    public int getID() {
        return ID;
    }
}
