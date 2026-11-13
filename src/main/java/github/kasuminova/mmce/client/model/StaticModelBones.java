package github.kasuminova.mmce.client.model;

import com.google.common.collect.ImmutableSet;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StaticModelBones {

    private final GeoModel      model;
    private final AnimationFile animationFile;
    private       Set<String>   staticBones    = new HashSet<>();
    private       Set<String>   staticSubtrees = new HashSet<>();

    private StaticModelBones(final GeoModel model, final AnimationFile animationFile) {
        this.model = model;
        this.animationFile = animationFile;
        compile();
    }

    public static StaticModelBones compile(final GeoModel model, final AnimationFile animation) {
        return new StaticModelBones(model, animation);
    }

    public void compile() {
        Set<String> animatedBones = animationFile == null
            ? Collections.emptySet()
            : animationFile.getAllAnimations().stream()
                           .flatMap(animation -> animation.boneAnimations.stream())
                           .map(boneAnimation -> boneAnimation.boneName)
                           .collect(ImmutableSet.toImmutableSet());

        for (final GeoBone bone : model.topLevelBones) {
            recursiveAdd(bone, animatedBones);
        }
        staticBones = ImmutableSet.copyOf(staticBones);
        staticSubtrees = ImmutableSet.copyOf(staticSubtrees);
    }

    private boolean recursiveAdd(final GeoBone bone, final Set<String> animatedBones) {
        if (animatedBones.contains(bone.name)) {
            return false;
        }

        staticBones.add(bone.name);
        boolean allChildrenStatic = true;
        for (final GeoBone childBone : bone.childBones) {
            if (!recursiveAdd(childBone, animatedBones)) {
                allChildrenStatic = false;
            }
        }
        if (allChildrenStatic) {
            staticSubtrees.add(bone.name);
        }
        return allChildrenStatic;
    }

    public boolean isStaticBone(final String boneName) {
        return staticBones.contains(boneName);
    }

    public boolean isStaticSubtree(final String boneName) {
        return staticSubtrees.contains(boneName);
    }

}
