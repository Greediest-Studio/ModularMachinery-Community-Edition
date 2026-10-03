// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.client.renderer.entity;

import hellfirepvp.modularmachinery.common.entity.EntityImprovedMeteor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class MeteorRenderFactory implements IRenderFactory<EntityImprovedMeteor> {
    @Override
    public Render<? super EntityImprovedMeteor> createRenderFor(RenderManager manager) {
        return new RenderEntityMeteorWrapper(manager);
    }
}
