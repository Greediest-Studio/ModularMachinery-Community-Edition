package github.kasuminova.mmce.client;

import github.kasuminova.mmce.client.model.ModelBufferSize;
import github.kasuminova.mmce.client.model.StaticModelBones;
import github.kasuminova.mmce.client.util.MatrixStack;
import org.junit.Test;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.core.keyframe.BoneAnimation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ClientOptimizationTest {

    private static GeoBone bone(String name) {
        GeoBone bone = new GeoBone();
        bone.name = name;
        if (bone.childBones == null) {
            bone.childBones = new ArrayList<>();
        }
        if (bone.childCubes == null) {
            bone.childCubes = new ArrayList<>();
        }
        return bone;
    }

    private static GeoCube cube(float rx, float ry, float rz) {
        try {
            Constructor<GeoCube> ctor = GeoCube.class.getDeclaredConstructor(double[].class);
            ctor.setAccessible(true);
            GeoCube cube = ctor.newInstance((Object) new double[]{1.0, 1.0, 1.0});
            cube.pivot = new Vector3f();
            cube.rotation = new Vector3f(rx, ry, rz);
            return cube;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to create GeoCube for test", e);
        }
    }

    @Test
    public void staticModelBonesIncludesLeafBonesAndSubtrees() {
        GeoBone staticRoot = bone("static_root");
        GeoBone staticLeaf = bone("static_leaf");
        staticLeaf.childCubes.add(cube(0.0F, 0.0F, 0.0F));
        staticRoot.childBones.add(staticLeaf);

        GeoBone mixedRoot = bone("mixed_root");
        GeoBone staticSiblingLeaf = bone("static_sibling_leaf");
        staticSiblingLeaf.childCubes.add(cube(0.0F, 0.0F, 0.0F));
        GeoBone animatedLeaf = bone("animated_leaf");
        animatedLeaf.childCubes.add(cube(0.0F, 0.0F, 0.0F));
        mixedRoot.childBones.add(staticSiblingLeaf);
        mixedRoot.childBones.add(animatedLeaf);

        GeoModel model = new GeoModel();
        model.topLevelBones.add(staticRoot);
        model.topLevelBones.add(mixedRoot);

        AnimationFile animFile = new AnimationFile();
        Animation anim = new Animation();
        anim.boneAnimations = new ArrayList<>();
        BoneAnimation boneAnim = new BoneAnimation();
        boneAnim.boneName = "animated_leaf";
        anim.boneAnimations.add(boneAnim);
        animFile.putAnimation("spin", anim);

        StaticModelBones compiled = StaticModelBones.compile(model, animFile);

        assertTrue(compiled.isStaticBone("static_root"));
        assertTrue(compiled.isStaticSubtree("static_root"));
        assertTrue(compiled.isStaticBone("static_leaf"));
        assertTrue(compiled.isStaticSubtree("static_leaf"));

        assertTrue(compiled.isStaticBone("mixed_root"));
        assertFalse(compiled.isStaticSubtree("mixed_root"));
        assertTrue(compiled.isStaticBone("static_sibling_leaf"));
        assertTrue(compiled.isStaticSubtree("static_sibling_leaf"));
        assertFalse(compiled.isStaticBone("animated_leaf"));
        assertFalse(compiled.isStaticSubtree("animated_leaf"));
    }

    @Test
    public void modelBufferSizeTracksDynamicStaticAndBloomCubes() {
        GeoBone staticBloom = bone("bloom_static_leaf");
        staticBloom.childCubes.add(cube(0.0F, 0.0F, 0.0F));

        GeoModel staticOnlyModel = new GeoModel();
        staticOnlyModel.topLevelBones.add(staticBloom);
        StaticModelBones staticOnlyBones = StaticModelBones.compile(staticOnlyModel, null);
        ModelBufferSize staticOnlySize = ModelBufferSize.calculate(staticOnlyModel, staticOnlyBones);

        assertTrue(staticOnlySize.hasStaticCubes());
        assertFalse(staticOnlySize.hasDynamicCubes());
        assertTrue(staticOnlySize.hasBloomCubes());
        assertFalse(staticOnlySize.hasDynamicBloomCubes());

        GeoBone animatedBloom = bone("bloom_rotator");
        animatedBloom.childCubes.add(cube(0.0F, 0.0F, 0.0F));
        GeoModel dynamicModel = new GeoModel();
        dynamicModel.topLevelBones.add(staticBloom);
        dynamicModel.topLevelBones.add(animatedBloom);

        AnimationFile animFile = new AnimationFile();
        Animation anim = new Animation();
        anim.boneAnimations = new ArrayList<>();
        BoneAnimation boneAnim = new BoneAnimation();
        boneAnim.boneName = "bloom_rotator";
        anim.boneAnimations.add(boneAnim);
        animFile.putAnimation("spin", anim);

        StaticModelBones dynamicBones = StaticModelBones.compile(dynamicModel, animFile);
        ModelBufferSize dynamicSize = ModelBufferSize.calculate(dynamicModel, dynamicBones);

        assertTrue(dynamicSize.hasStaticCubes());
        assertTrue(dynamicSize.hasDynamicCubes());
        assertTrue(dynamicSize.hasBloomCubes());
        assertTrue(dynamicSize.hasDynamicBloomCubes());
    }

    @Test
    public void matrixStackPoolsMatricesAndMatchesCubeRotation() {
        MatrixStack stack = new MatrixStack();
        Matrix4f expectedIdentity = new Matrix4f();
        expectedIdentity.setIdentity();
        Matrix3f expectedNormalIdentity = new Matrix3f();
        expectedNormalIdentity.setIdentity();

        assertSame(stack.getTempRenderNormal(), stack.getTempRenderNormal());
        assertSame(stack.getTempRenderVertex(), stack.getTempRenderVertex());

        stack.translate(0.0F, 0.0F, 0.0F);
        stack.scale(1.0F, 1.0F, 1.0F);
        stack.rotate(cube(0.0F, 0.0F, 0.0F));
        assertEquals(expectedIdentity, stack.getModelMatrix());
        assertEquals(expectedNormalIdentity, stack.getNormalMatrix());

        // Exercise push/pop pool reuse across multiple cycles.
        for (int i = 0; i < 4; i++) {
            stack.push();
            stack.translate(1.5F, -0.5F, 2.0F);
            stack.scale(2.0F, 0.5F, -1.0F);
            stack.rotate(cube(0.3F, 0.5F, 0.7F));
            stack.pop();
            assertEquals(expectedIdentity, stack.getModelMatrix());
            assertEquals(expectedNormalIdentity, stack.getNormalMatrix());
        }

        MatrixStack reference = new MatrixStack();
        reference.rotateZ(0.7F);
        reference.rotateY(0.5F);
        reference.rotateX(0.3F);
        stack.rotate(cube(0.3F, 0.5F, 0.7F));
        assertEquals(reference.getModelMatrix(), stack.getModelMatrix());
        assertEquals(reference.getNormalMatrix(), stack.getNormalMatrix());
    }
}
