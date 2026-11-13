package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.client.model.ModelBufferSize;
import github.kasuminova.mmce.client.util.BufferBuilderPool;
import github.kasuminova.mmce.client.util.BufferProvider;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.RecursiveAction;

@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class GeoModelRenderTask extends RecursiveAction implements BufferProvider {
    protected final MachineControllerRenderer       renderer;
    protected final TileMultiblockMachineController ctrl;

    protected volatile BufferBuilder buffer;
    protected volatile BufferBuilder bloomBuffer;
    protected volatile BufferBuilder transparentBuffer;
    protected volatile BufferBuilder bloomTransparentBuffer;

    protected int bufferSize                 = 0;
    protected int bloomBufferSize            = 0;
    protected int transparentBufferSize      = 0;
    protected int bloomTransparentBufferSize = 0;

    protected volatile BufferBuilder staticBuffer;
    protected volatile BufferBuilder staticBloomBuffer;
    protected volatile BufferBuilder staticTransparentBuffer;
    protected volatile BufferBuilder staticBloomTransparentBuffer;

    protected int staticBufferSize                 = 0;
    protected int staticBloomBufferSize            = 0;
    protected int staticTransparentBufferSize      = 0;
    protected int staticBloomTransparentBufferSize = 0;

    protected boolean available = true;
    protected boolean bloomRegistered = false;
    protected boolean hasDynamicCubes = false;
    protected boolean hasStaticCubes  = false;
    protected boolean hasBloomCubes   = false;
    protected volatile boolean submittedAsync  = false;
    protected volatile boolean dynamicRendered = false;
    protected volatile boolean cancelled       = false;

    protected MachineControllerModel currentModel = null;
    protected MachineControllerModel lastComputedModel = null;
    protected EnumFacing             renderedControllerRotation = null;

    public GeoModelRenderTask(final MachineControllerRenderer renderer, final TileMultiblockMachineController ctrl) {
        this.renderer = renderer;
        this.ctrl = ctrl;
    }

    private void registerBloomIfNeeded() {
        if (bloomRegistered || !hasBloomCubes) {
            return;
        }
        bloomRegistered = true;
        if (Mods.GREGTECHCEU.isPresent()) {
            registerBloomRenderer();
        } else if (Mods.LUMENIZED.isPresent()) {
            registerBloomRendererLumenized();
        }
    }

    @SideOnly(Side.CLIENT)
    @net.minecraftforge.fml.common.Optional.Method(modid = "gregtech")
    public void registerBloomRenderer() {
        BloomGeoModelRenderer.INSTANCE.registerGlobal(ctrl);
    }

    @SideOnly(Side.CLIENT)
    @net.minecraftforge.fml.common.Optional.Method(modid = "lumenized")
    public void registerBloomRendererLumenized() {
        BloomGeoModelRenderer.INSTANCE.registerGlobal(ctrl);
    }

    public void submitInitial(final MachineControllerModel model) {
        calculateBufferSize(model);
        if (!hasDynamicCubes) {
            dynamicRendered = true;
            submittedAsync = false;
            return;
        }
        if (Config.asyncControllerModelRender) {
            submittedAsync = true;
            TaskExecutor.FORK_JOIN_POOL.submit(this);
        } else {
            submittedAsync = false;
            computeSync(model);
        }
    }

    public boolean isSubmittedAsync() {
        return submittedAsync;
    }

    public boolean hasDynamicCubes() {
        return hasDynamicCubes;
    }

    public boolean hasStaticCubes() {
        return hasStaticCubes;
    }

    public boolean hasBloomCubes() {
        return hasBloomCubes;
    }

    public boolean isDynamicRendered() {
        return dynamicRendered;
    }

    public synchronized void computeSync(final MachineControllerModel model) {
        if (cancelled) {
            return;
        }
        try {
            if (lastComputedModel != model) {
                calculateBufferSize(model);
            }
            if (hasDynamicCubes) {
                renderer.render(ctrl, this, false, model);
            }
            dynamicRendered = true;
        } catch (Throwable e) {
            ModularMachinery.log.warn("[MMCE-Render] Failed to render controller model!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }
    }

    @Override
    protected synchronized void compute() {
        if (cancelled) {
            return;
        }
        try {
            MachineControllerModel model = lastComputedModel != null ? lastComputedModel : ctrl.getCurrentModel();
            if (model == null) {
                return;
            }
            if (lastComputedModel != model) {
                calculateBufferSize(model);
            }
            if (hasDynamicCubes) {
                renderer.render(ctrl, this, false, model);
            }
            dynamicRendered = true;
        } catch (Throwable e) {
            ModularMachinery.log.warn("[MMCE-AsyncRender] Failed to render controller model!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }
    }

    public void renderAll() {
        renderAll(ctrl.getCurrentModel());
    }

    public void renderAll(final MachineControllerModel model) {
        if (model == null) {
            return;
        }
        renderDefault(model, false);
        renderBloom(true, model);
    }

    public void renderDefault() {
        renderDefault(ctrl.getCurrentModel(), false);
    }

    public void renderDefault(final MachineControllerModel model) {
        renderDefault(model, false);
    }

    public void renderDefault(final MachineControllerModel model, final boolean deferDynamicReinitToBloom) {
        if (model == null) {
            return;
        }
        ResourceLocation texture = model.getTextureLocation();
        if (buffer != null || transparentBuffer != null || staticBuffer != null || staticTransparentBuffer != null) {
            int combinedLight = ctrl.getWorld().getCombinedLight(ctrl.getPos(), 0);

            if (buffer != null) {
                ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.DEFAULT, texture, buffer);
            }
            if (transparentBuffer != null) {
                ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.TRANSPARENT, texture, transparentBuffer);
            }
            if (staticBuffer != null) {
                ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.DEFAULT, texture, staticBuffer);
            }
            if (staticTransparentBuffer != null) {
                ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.TRANSPARENT, texture, staticTransparentBuffer);
            }
        }

        // If this controller has no bloom geometry, bloom pass will never run reinitialize for it.
        if (hasDynamicCubes && (!deferDynamicReinitToBloom || !hasAnyBloomBuffer())) {
            ControllerModelRenderManager.INSTANCE.addReinitializeCallback(this, this::prepareNextDynamicFrame);
        }
    }

    private boolean hasAnyBloomBuffer() {
        return bloomBuffer != null || bloomTransparentBuffer != null || staticBloomBuffer != null || staticBloomTransparentBuffer != null;
    }

    public void renderBloom(boolean postProcessing) {
        renderBloom(postProcessing, ctrl.getCurrentModel());
    }

    public void renderBloom(boolean postProcessing, final MachineControllerModel model) {
        if (!hasAnyBloomBuffer()) {
            if (postProcessing) {
                available = false;
            }
            return;
        }
        if (model == null) {
            if (postProcessing) {
                available = false;
            }
            return;
        }
        ResourceLocation texture = model.getTextureLocation();

        if (bloomBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM, texture, bloomBuffer);
        }
        if (bloomTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM_TRANSPARENT, texture, bloomTransparentBuffer);
        }
        if (staticBloomBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM, texture, staticBloomBuffer);
        }
        if (staticBloomTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM_TRANSPARENT, texture, staticBloomTransparentBuffer);
        }

        if (postProcessing) {
            if (hasDynamicCubes) {
                ControllerModelRenderManager.INSTANCE.addReinitializeCallback(this, this::prepareNextDynamicFrame);
            } else {
                available = false;
            }
        }
    }

    public void prepareNextDynamicFrame() {
        clean();
        available = false;
        dynamicRendered = false;
        if (cancelled || !hasDynamicCubes) {
            return;
        }
        if (Config.asyncControllerModelRender) {
            submittedAsync = true;
            super.reinitialize();
            TaskExecutor.FORK_JOIN_POOL.submit(this);
        } else {
            submittedAsync = false;
        }
    }

    private void calculateBufferSize() {
        calculateBufferSize(ctrl.getCurrentModel());
    }

    private void calculateBufferSize(final MachineControllerModel model) {
        lastComputedModel = model;
        if (model != null) {
            ModelBufferSize modelBufferSize = model.getBufferSize();
            bufferSize = modelBufferSize.getBufferSize();
            bloomBufferSize = modelBufferSize.getBloomBufferSize();
            transparentBufferSize = modelBufferSize.getTransparentBufferSize();
            bloomTransparentBufferSize = modelBufferSize.getBloomTransparentBufferSize();
            staticBufferSize = modelBufferSize.getStaticBufferSize();
            staticBloomBufferSize = modelBufferSize.getStaticBloomBufferSize();
            staticTransparentBufferSize = modelBufferSize.getStaticTransparentBufferSize();
            staticBloomTransparentBufferSize = modelBufferSize.getStaticBloomTransparentBufferSize();
            hasDynamicCubes = modelBufferSize.hasDynamicCubes();
            hasStaticCubes = modelBufferSize.hasStaticCubes();
            hasBloomCubes = modelBufferSize.hasBloomCubes();
            registerBloomIfNeeded();
        } else {
            bufferSize = 0;
            bloomBufferSize = 0;
            transparentBufferSize = 0;
            bloomTransparentBufferSize = 0;
            staticBufferSize = 0;
            staticBloomBufferSize = 0;
            staticTransparentBufferSize = 0;
            staticBloomTransparentBufferSize = 0;
            hasDynamicCubes = false;
            hasStaticCubes = false;
            hasBloomCubes = false;
        }
    }

    @Override
    public void reinitialize() {
        clean();
        available = false;
        dynamicRendered = false;
        super.reinitialize();
    }

    public synchronized void cleanup() {
        cancelled = true;
        clean();
        cleanStatic();
    }

    @Override
    public BufferBuilder getBuffer() {
        return buffer;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public boolean shouldReRenderStatic() {
        return shouldReRenderStatic(ctrl.getCurrentModel());
    }

    public boolean shouldReRenderStatic(final MachineControllerModel nextModel) {
        EnumFacing currentRotation = ctrl.getControllerRotation();
        return currentModel != nextModel || renderedControllerRotation != currentRotation;
    }

    public void handleStaticChange(final MachineControllerModel nextModel) {
        boolean initialStaticRender = currentModel == null && renderedControllerRotation == null;
        boolean modelChanged = lastComputedModel != nextModel;
        currentModel = nextModel;
        renderedControllerRotation = ctrl.getControllerRotation();

        if (!initialStaticRender) {
            cleanStatic();
            clean();
            dynamicRendered = false;
        }
        if (modelChanged) {
            calculateBufferSize(nextModel);
        }
        if (hasStaticCubes) {
            renderer.render(ctrl, this, true, nextModel);
        }
        if (hasDynamicCubes && !dynamicRendered) {
            computeSync(nextModel);
        }
    }

    @Override
    public BufferBuilder getBuffer(final boolean bloom, final boolean transparent, final boolean isStatic) {
        // BLOOM + TRANSPARENT
        if (bloom && transparent) {
            // Static render
            if (isStatic) {
                // Dynamic render
                if (staticBloomTransparentBuffer == null) {
                    staticBloomTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBloomTransparentBufferSize, 1024));
                    staticBloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticBloomTransparentBuffer;
            }
            // Dynamic render
            if (bloomTransparentBuffer == null) {
                bloomTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(bloomTransparentBufferSize, 1024));
                bloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomTransparentBuffer;
        }
        // BLOOM
        if (bloom) {
            // Static render
            if (isStatic) {
                if (staticBloomBuffer == null) {
                    staticBloomBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBloomBufferSize, 2048));
                    staticBloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticBloomBuffer;
            }
            // Dynamic render
            if (bloomBuffer == null) {
                bloomBuffer = BufferBuilderPool.borrowBuffer(Math.max(bloomBufferSize, 2048));
                bloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomBuffer;
        }
        // TRANSPARENT
        if (transparent) {
            // Static render
            if (isStatic) {
                if (staticTransparentBuffer == null) {
                    staticTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticTransparentBufferSize, 2048));
                    staticTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticTransparentBuffer;
            }
            // Dynamic render
            if (transparentBuffer == null) {
                transparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(transparentBufferSize, 2048));
                transparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return transparentBuffer;
        }
        // Static render
        if (isStatic) {
            if (staticBuffer == null) {
                staticBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBufferSize, 4096));
                staticBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return staticBuffer;
        }
        // Dynamic render
        if (buffer == null) {
            buffer = BufferBuilderPool.borrowBuffer(Math.max(bufferSize, 4096));
            buffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        return buffer;
    }

    @Override
    public void begin(final boolean isStatic) {
        if (isStatic) {
            if (staticBuffer != null) {
                staticBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticBloomBuffer != null) {
                staticBloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticTransparentBuffer != null) {
                staticTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticBloomTransparentBuffer != null) {
                staticBloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return;
        }

        if (buffer != null) {
            buffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (transparentBuffer != null) {
            transparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (bloomBuffer != null) {
            bloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (bloomTransparentBuffer != null) {
            bloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
    }

    @Override
    public void finishDrawing(final boolean isStatic) {
        if (isStatic) {
            if (staticBuffer != null) {
                staticBuffer.finishDrawing();
            }
            if (staticBloomBuffer != null) {
                staticBloomBuffer.finishDrawing();
            }
            if (staticTransparentBuffer != null) {
                staticTransparentBuffer.finishDrawing();
            }
            if (staticBloomTransparentBuffer != null) {
                staticBloomTransparentBuffer.finishDrawing();
            }
            return;
        }

        if (buffer != null) {
            buffer.finishDrawing();
        }
        if (bloomBuffer != null) {
            bloomBuffer.finishDrawing();
        }
        if (transparentBuffer != null) {
            transparentBuffer.finishDrawing();
        }
        if (bloomTransparentBuffer != null) {
            bloomTransparentBuffer.finishDrawing();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        if (buffer == null && bloomBuffer == null && transparentBuffer == null && bloomTransparentBuffer == null &&
            staticBuffer == null && staticBloomBuffer == null && staticTransparentBuffer == null && staticBloomTransparentBuffer == null) {
            super.finalize();
            return;
        }

        Minecraft.getMinecraft().addScheduledTask(() -> {
            clean();
            cleanStatic();
        });
        super.finalize();
    }

    private synchronized void clean() {
        if (buffer != null) {
            BufferBuilderPool.returnBuffer(buffer);
            buffer = null;
        }
        if (bloomBuffer != null) {
            BufferBuilderPool.returnBuffer(bloomBuffer);
            bloomBuffer = null;
        }
        if (transparentBuffer != null) {
            BufferBuilderPool.returnBuffer(transparentBuffer);
            transparentBuffer = null;
        }
        if (bloomTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(bloomTransparentBuffer);
            bloomTransparentBuffer = null;
        }
    }

    private synchronized void cleanStatic() {
        if (staticBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBuffer);
            staticBuffer = null;
        }
        if (staticBloomBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBloomBuffer);
            staticBloomBuffer = null;
        }
        if (staticTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(staticTransparentBuffer);
            staticTransparentBuffer = null;
        }
        if (staticBloomTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBloomTransparentBuffer);
            staticBloomTransparentBuffer = null;
        }
    }

}
