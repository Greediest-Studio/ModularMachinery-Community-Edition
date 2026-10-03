// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.client.renderer.entity;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.client.render.entity.RenderEntityMeteor;
import hellfirepvp.modularmachinery.common.entity.EntityImprovedMeteor;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RenderEntityMeteorWrapper extends Render<EntityImprovedMeteor> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BloodMagic.MODID, "textures/models/Meteor.png");

    private final RenderEntityMeteor renderEntityMeteor;

    protected RenderEntityMeteorWrapper(RenderManager renderManager) {
        super(renderManager);
        renderEntityMeteor = new RenderEntityMeteor(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityImprovedMeteor entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityImprovedMeteor entity, double x, double y, double z, float entityYaw, float partialTicks) {
        renderEntityMeteor.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
