package hellfirepvp.modularmachinery.mixin.minecraft;

import hellfirepvp.modularmachinery.client.shader.ShaderManager;
import hellfirepvp.modularmachinery.client.renderer.ControllerModelRenderManager;
import hellfirepvp.modularmachinery.client.renderer.MachineControllerRenderer;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "setWorldAndLoadRenderers", at = @At("HEAD"))
    private void clearControllerModels(final WorldClient world, final CallbackInfo ci) {
        if (Mods.GECKOLIB.isPresent()) {
            ControllerModelRenderManager.INSTANCE.clear();
        }
    }

    @Inject(method = "renderEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void hookTESRComplete(final Entity renderViewEntity, final ICamera camera, final float partialTicks, final CallbackInfo ci) {
        // Use RenderPass 0, prevent twice render.
        if (Mods.GECKOLIB.isPresent() && MinecraftForgeClient.getRenderPass() == 0) {
            if (MachineControllerRenderer.shouldUseBloom() && !ShaderManager.isOptifineShaderPackLoaded()) {
                ControllerModelRenderManager.INSTANCE.draw();
            } else {
                ControllerModelRenderManager.INSTANCE.draw();
                ControllerModelRenderManager.INSTANCE.checkControllerState();
            }
        }
    }

}
