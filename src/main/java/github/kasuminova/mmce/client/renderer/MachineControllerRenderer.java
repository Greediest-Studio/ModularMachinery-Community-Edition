package github.kasuminova.mmce.client.renderer;

import com.cleanroommc.client.shader.ShaderManager;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.client.model.StaticModelBones;
import github.kasuminova.mmce.client.util.BufferProvider;
import github.kasuminova.mmce.client.util.MatrixStack;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MachineControllerRenderer extends TileEntitySpecialRenderer<TileMultiblockMachineController> {

    public static final MachineControllerRenderer INSTANCE = new MachineControllerRenderer();

    public static final VertexFormat VERTEX_FORMAT = DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;

    protected static final ThreadLocal<MatrixStack>      MATRIX_STACK       = ThreadLocal.withInitial(MatrixStack::new);
    protected static final ThreadLocal<StaticModelBones> STATIC_MODEL_BONES = new ThreadLocal<>();

    static {
        if (Mods.GECKOLIB.isPresent()) {
            AnimationController.addModelFetcher((IAnimatable object) -> {
                if (object instanceof TileMultiblockMachineController ctrl) {
                    MachineControllerModel currentModel = ctrl.getCurrentModel();
                    if (currentModel != null) {
                        return (IAnimatableModel) currentModel;
                    }
                }
                return null;
            });
        }
    }

    protected final Map<TileMultiblockMachineController, GeoModelRenderTask> tasks = new WeakHashMap<>();

    protected MachineControllerRenderer() {
    }

    protected static void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH -> GlStateManager.rotate(180, 0, 1, 0);
            case WEST -> GlStateManager.rotate(90, 0, 1, 0);
            /* There is no need to rotate by 0 */
            case NORTH -> {
            }
            case EAST -> GlStateManager.rotate(270, 0, 1, 0);
            case UP -> GlStateManager.rotate(90, 1, 0, 0);
            case DOWN -> GlStateManager.rotate(90, -1, 0, 0);
        }
    }

    protected static void rotateBlockMatrix(EnumFacing facing) {
        switch (facing) {
            // 180
            case SOUTH -> MATRIX_STACK.get().rotateY(3.141592653589793f);
            // 90
            case WEST -> MATRIX_STACK.get().rotateY(1.5707963267948966f);
            /* There is no need to rotate by 0 */
            case NORTH -> {
            }
            // 270
            case EAST -> MATRIX_STACK.get().rotateY(4.71238898038469f);
        }
    }

    public static boolean shouldUseBloom() {
        return Mods.GREGTECHCEU.isPresent() || Mods.LUMENIZED.isPresent();
    }

    @Override
    public void render(TileMultiblockMachineController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        this.render(te, x, y, z, partialTicks, destroyStage);
    }

    public void renderDummy(TileMultiblockMachineController ctrl, MachineControllerModel model) {
        rendererDispatcher.renderEngine.bindTexture(model.getTextureLocation());
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.01f, 0);
        GlStateManager.translate(0.5f, 0, 0.5f);

        renderWithDefault(model, ctrl);

        GlStateManager.popMatrix();
    }

    @Optional.Method(modid = "geckolib3")
    public void render(TileMultiblockMachineController tile, double x, double y, double z, float partialTicks, int destroyStage) {
        MachineControllerModel modelProvider = tile.getCurrentModel();
        if (modelProvider == null) {
            return;
        }

        render(modelProvider, tile, x, y, z, partialTicks);
    }

    @Optional.Method(modid = "geckolib3")
    public void render(final MachineControllerModel modelProvider,
                       final TileMultiblockMachineController tile,
                       double x, double y, double z,
                       final float partialTicks) {
        renderWithBuffer(tile, modelProvider);
    }

    @Optional.Method(modid = "geckolib3")
    private void renderWithBuffer(final TileMultiblockMachineController animatable, final MachineControllerModel modelProvider) {
        ControllerModelRenderManager.INSTANCE.addToRender(animatable);
        GeoModelRenderTask task = getTask(animatable, modelProvider);

        if (task.shouldReRenderStatic(modelProvider)) {
            task.handleStaticChange(modelProvider);
        } else if (!Config.asyncControllerModelRender && task.hasDynamicCubes() && !task.isDynamicRendered()) {
            task.computeSync(modelProvider);
        }

        if (MachineControllerRenderer.shouldUseBloom() && !ShaderManager.isOptifineShaderPackLoaded()) {
            task.setAvailable(true);
            task.renderDefault(modelProvider, true);
        } else {
            task.renderAll(modelProvider);
        }
    }

    @Optional.Method(modid = "geckolib3")
    private void renderWithDefault(final MachineControllerModel modelProvider,
                                   final TileMultiblockMachineController ctrl) {
        GeoModel model = modelProvider.getModel();
        modelProvider.setLivingAnimations(ctrl, ctrl.hashCode());

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL11.GL_QUADS, VERTEX_FORMAT);

        // Render all top level bones
        for (GeoBone group : model.topLevelBones) {
            renderRecursively(builder, group, 1F, 1F, 1F, 1F);
        }

        Tessellator.getInstance().draw();
    }

    @Optional.Method(modid = "geckolib3")
    public void render(TileMultiblockMachineController tile,
                       BufferProvider bufferProvider,
                       boolean renderStatic) {
        render(tile, bufferProvider, renderStatic, tile.getCurrentModel());
    }

    @Optional.Method(modid = "geckolib3")
    public void render(TileMultiblockMachineController tile,
                       BufferProvider bufferProvider,
                       boolean renderStatic,
                       MachineControllerModel modelProvider) {
        if (modelProvider == null) {
            return;
        }
        MachineControllerModel renderInst = modelProvider.getRenderInstance();
        GeoModel model = renderInst.getModel();
        StaticModelBones staticModelBones = renderInst.getStaticModelBones();
        STATIC_MODEL_BONES.set(staticModelBones);
        try {
            synchronized (model) {
                renderInst.setLivingAnimations(tile, tile.hashCode());
                bufferProvider.begin(renderStatic);

                MatrixStack matrixStack = MATRIX_STACK.get();
                BlockPos pos = tile.getPos();
                matrixStack.push();
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                matrixStack.translate(0, 0.01f, 0);
                matrixStack.translate(0.5f, 0, 0.5f);
                rotateBlockMatrix(tile.getControllerRotation(), matrixStack);

                // Render all top level bones
                for (GeoBone group : model.topLevelBones) {
                    if (group.isHidden() && group.childBonesAreHiddenToo()) {
                        continue;
                    }
                    renderRecursively(bufferProvider, group, 1F, 1F, 1F, 1F, false, false, renderStatic, staticModelBones, matrixStack);
                }

                matrixStack.pop();

                bufferProvider.finishDrawing(renderStatic);
            }
        } finally {
            renderInst.returnRenderInst();
        }
    }

    protected static void rotateBlockMatrix(EnumFacing facing, MatrixStack matrixStack) {
        switch (facing) {
            // 180
            case SOUTH -> matrixStack.rotateY(3.141592653589793f);
            // 90
            case WEST -> matrixStack.rotateY(1.5707963267948966f);
            /* There is no need to rotate by 0 */
            case NORTH -> {
            }
            // 270
            case EAST -> matrixStack.rotateY(4.71238898038469f);
        }
    }

    @Optional.Method(modid = "geckolib3")
    public void renderRecursively(BufferBuilder buffer, GeoBone bone,
                                  float red, float green, float blue, float alpha) {
        boolean emissive = bone.name.startsWith("emissive") || bone.name.startsWith("bloom");
        boolean transparent = bone.name.startsWith("transparent") || bone.name.startsWith("emissive_transparent") || bone.name.startsWith("bloom_transparent");
        float lastBrightnessX = 0;
        float lastBrightnessY = 0;
        if (emissive || transparent) {
            Tessellator.getInstance().draw();
            if (emissive) {
                GlStateManager.doPolygonOffset(-3.0F, -3.0F);
                GlStateManager.enablePolygonOffset();
                lastBrightnessX = OpenGlHelper.lastBrightnessX;
                lastBrightnessY = OpenGlHelper.lastBrightnessY;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            }
            if (transparent) {
                GlStateManager.depthMask(false);
            }
            buffer.begin(GL11.GL_QUADS, VERTEX_FORMAT);
        }

        MatrixStack matrixStack = MATRIX_STACK.get();
        matrixStack.push();

        matrixStack.translate(bone);
        matrixStack.moveToPivot(bone);
        matrixStack.rotate(bone);
        matrixStack.scale(bone);
        matrixStack.moveBackFromPivot(bone);

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.childCubes) {
                matrixStack.push();
                renderCube(buffer, cube, red, green, blue, alpha);
                matrixStack.pop();
            }
        }
        if (!bone.childBonesAreHiddenToo()) {
            for (GeoBone childBone : bone.childBones) {
                renderRecursively(buffer, childBone, red, green, blue, alpha);
            }
        }

        matrixStack.pop();
        if (emissive || transparent) {
            Tessellator.getInstance().draw();
            if (emissive) {
                GlStateManager.doPolygonOffset(0.0F, 0.0F);
                GlStateManager.disablePolygonOffset();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
            }
            if (transparent) {
                GlStateManager.depthMask(true);
            }
            buffer.begin(GL11.GL_QUADS, VERTEX_FORMAT);
        }
    }

    @Optional.Method(modid = "geckolib3")
    public void renderRecursively(BufferProvider bufferProvider,
                                  GeoBone bone,
                                  float red, float green, float blue, float alpha,
                                  boolean bloom, boolean transparent, boolean isStatic) {
        renderRecursively(bufferProvider, bone, red, green, blue, alpha, bloom, transparent, isStatic,
            STATIC_MODEL_BONES.get(), MATRIX_STACK.get());
    }

    @Optional.Method(modid = "geckolib3")
    private void renderRecursively(BufferProvider bufferProvider,
                                   GeoBone bone,
                                   float red, float green, float blue, float alpha,
                                   boolean bloom, boolean transparent, boolean isStatic,
                                   StaticModelBones staticModelBones,
                                   MatrixStack matrixStack) {
        boolean isBoneStatic = staticModelBones.isStaticBone(bone.name);
        if (isStatic) {
            if (!isBoneStatic) {
                return;
            }
        } else if (staticModelBones.isStaticSubtree(bone.name)) {
            return;
        }

        bloom |= bone.name.startsWith("emissive") || bone.name.startsWith("bloom");
        transparent |= bone.name.startsWith("transparent") || bone.name.startsWith("emissive_transparent") || bone.name.startsWith("bloom_transparent");

        matrixStack.push();

        matrixStack.translate(bone);
        matrixStack.moveToPivot(bone);
        matrixStack.rotate(bone);
        matrixStack.scale(bone);
        matrixStack.moveBackFromPivot(bone);

        if (!bone.isHidden() && !bone.childCubes.isEmpty() && isStatic == isBoneStatic) {
            BufferBuilder buffer = bufferProvider.getBuffer(bloom, transparent, isStatic);
            for (GeoCube cube : bone.childCubes) {
                boolean rotated = hasCubeRotation(cube);
                if (rotated) {
                    matrixStack.push();
                    renderCube(buffer, cube, red, green, blue, alpha, matrixStack, true);
                    matrixStack.pop();
                } else {
                    renderCube(buffer, cube, red, green, blue, alpha, matrixStack, false);
                }
            }
        }
        if (!bone.childBonesAreHiddenToo()) {
            for (GeoBone childBone : bone.childBones) {
                renderRecursively(bufferProvider, childBone, red, green, blue, alpha, bloom, transparent, isStatic, staticModelBones, matrixStack);
            }
        }

        matrixStack.pop();
    }

    private static boolean hasCubeRotation(final GeoCube cube) {
        Vector3f rotation = cube.rotation;
        return rotation.x != 0.0F || rotation.y != 0.0F || rotation.z != 0.0F;
    }

    @Optional.Method(modid = "geckolib3")
    public void renderCube(final BufferBuilder builder,
                           final GeoCube cube,
                           final float red, final float green, final float blue, final float alpha) {
        renderCube(builder, cube, red, green, blue, alpha, MATRIX_STACK.get(), hasCubeRotation(cube));
    }

    @Optional.Method(modid = "geckolib3")
    private void renderCube(final BufferBuilder builder,
                            final GeoCube cube,
                            final float red, final float green, final float blue, final float alpha,
                            final MatrixStack matrixStack,
                            final boolean rotated) {
        if (rotated) {
            matrixStack.moveToPivot(cube);
            matrixStack.rotate(cube);
            matrixStack.moveBackFromPivot(cube);
        }

        Vector3f normal = matrixStack.getTempRenderNormal();
        Vector4f vector4f = matrixStack.getTempRenderVertex();

        for (GeoQuad quad : cube.quads) {
            if (quad == null) {
                continue;
            }

            normal.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            matrixStack.getNormalMatrix().transform(normal);

            if ((cube.size.y == 0 || cube.size.z == 0) && normal.getX() < 0) {
                normal.x *= -1;
            }
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.getY() < 0) {
                normal.y *= -1;
            }
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.getZ() < 0) {
                normal.z *= -1;
            }

            for (GeoVertex vertex : quad.vertices) {
                vector4f.set(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
                matrixStack.getModelMatrix().transform(vector4f);

                builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ())
                       .tex(vertex.textureU, vertex.textureV)
                       .color(red, green, blue, alpha)
                       .normal(normal.getX(), normal.getY(), normal.getZ())
                       .endVertex();
            }
        }
    }

    @Nonnull
    public GeoModelRenderTask getTask(final TileMultiblockMachineController animatable) {
        return getTask(animatable, animatable.getCurrentModel());
    }

    @Nonnull
    public GeoModelRenderTask getTask(final TileMultiblockMachineController animatable,
                                      final MachineControllerModel modelProvider) {
        GeoModelRenderTask task = tasks.get(animatable);
        if (task == null) {
            task = new GeoModelRenderTask(this, animatable);
            tasks.put(animatable, task);
            task.submitInitial(modelProvider);
        }
        if (task.isSubmittedAsync() && !task.isDone()) {
            long current = System.currentTimeMillis();
            task.join();
            long after = System.currentTimeMillis();
            if (after - current > 50) {
                ModularMachinery.log.warn("[MMCE-AsyncRender] GeoModelRenderTask took too long to complete! ({}ms)", after - current);
            }
        }
        return task;
    }

    @Override
    public boolean isGlobalRenderer(final TileMultiblockMachineController te) {
        return true;
    }

}
