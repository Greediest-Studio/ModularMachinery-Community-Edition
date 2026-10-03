package hellfirepvp.modularmachinery.common.integration;

import hellfirepvp.modularmachinery.common.integration.jei.helper.BiomeHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.DimensionHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.DragonBreathHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.FluxHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.HeatHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.LaserHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.MeteorHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.PotentialEnergyHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.RadiationHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.VisHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Biome;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DragonBreath;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Flux;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.LocalizationHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.LocalizationKeys;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import hellfirepvp.modularmachinery.common.integration.jei.render.BiomeRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.DimensionRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.DragonBreathRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.FluxRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.HeatRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.LaserRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.MeteorRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.PotentialEnergyRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.RadiationRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.VisRenderer;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.integration.jei.helper.AspectHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.AuraHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.ConstellationHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.DemonWillHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.GridHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.ImpetusHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.LifeEssenceHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.ManaHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.RainbowHelper;
import hellfirepvp.modularmachinery.common.integration.jei.helper.StarlightHelper;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Aura;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Constellation;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DemonWill;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Grid;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Impetus;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.LifeEssence;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Mana;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Rainbow;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Starlight;
import hellfirepvp.modularmachinery.common.integration.jei.render.AspectRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.AuraRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.ConstellationRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.DemonWillRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.GridRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.ImpetusRender;
import hellfirepvp.modularmachinery.common.integration.jei.render.LifeEssenceRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.ManaRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.RainbowRenderer;
import hellfirepvp.modularmachinery.common.integration.jei.render.StarlightRenderer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nonnull;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

    public static IGuiHelper GUI_HELPER;

    @Override
    public void register(IModRegistry registry) {
        GUI_HELPER = registry.getJeiHelpers().getGuiHelper();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        if (Mods.BM2.isPresent()) {
            registry.register(DemonWill.class, Lists.newArrayList(), new DemonWillHelper<>(), new DemonWillRenderer());
            registry.register(LifeEssence.class, Lists.newArrayList(), new LifeEssenceHelper<>(), new LifeEssenceRenderer());
        }
        // Multiblocked 和 Thaumic JEI 已注册 AspectList，HEI 不允许重复注册同一类型。
        if (Mods.TC6.isPresent() && !Mods.TAHUMIC_JEI.isPresent() && !Mods.MBD.isPresent()) {
            registry.register(AspectList.class, Lists.newArrayList(), new AspectHelper<>(), new AspectRenderer());
        }
        if (Mods.EXU2.isPresent()) {
            registry.register(Grid.class, Lists.newArrayList(), new GridHelper<>(), new GridRenderer());
            registry.register(Rainbow.class, Lists.newArrayList(), new RainbowHelper<>(), new RainbowRenderer());
        }
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            registry.register(Starlight.class, Lists.newArrayList(), new StarlightHelper<>(), new StarlightRenderer());
            registry.register(Constellation.class, Lists.newArrayList(), new ConstellationHelper<>(), new ConstellationRenderer());
        }
        if (Mods.NATURESAURA.isPresent()) {
            registry.register(Aura.class, Lists.newArrayList(), new AuraHelper<>(), new AuraRenderer());
        }
        if (Mods.BOTANIA.isPresent()) {
            registry.register(Mana.class, Lists.newArrayList(), new ManaHelper<Mana>(), new ManaRenderer());
        }
        if (Mods.TA.isPresent()) {
            registry.register(Impetus.class, Lists.newArrayList(), new ImpetusHelper<>(), new ImpetusRender());
        }

        registerIngredient(registry, new Biome(), new BiomeHelper(), new BiomeRenderer());
        registerIngredient(registry, new Dimension(), new DimensionHelper(), new DimensionRenderer());

        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            registerIngredient(registry, new Radiation(), new RadiationHelper(), new RadiationRenderer());
        }

        if (Mods.BM2.isPresent()) {
            registerIngredient(registry, new Meteor(), new MeteorHelper(), new MeteorRenderer());
        }

        if (Mods.TC6.isPresent()) {
            registerIngredient(registry, new Flux(0, 0), new FluxHelper(), new FluxRenderer());
            registerIngredient(registry, new Vis(), new VisHelper(), new VisRenderer());
        }

        if (Mods.ABYSSALCRAFT.isPresent()) {
            registerIngredient(registry, new PotentialEnergy(), new PotentialEnergyHelper(), new PotentialEnergyRenderer());
        }

        if (Mods.ICE_AND_FIRE.isPresent()) {
            registerIngredient(registry, new DragonBreath(), new DragonBreathHelper(), new DragonBreathRenderer());
        }

        if (Mods.MEKANISM.isPresent()) {
            registerIngredient(registry, new Laser(0), new LaserHelper(), new LaserRenderer());
            registerIngredient(registry, new Heat(0), new HeatHelper(), new HeatRenderer());
        }

    }

    private static <T> void registerIngredient(IModIngredientRegistration registry, IIngredientType<T> type,
                                              IIngredientHelper<T> helper, IIngredientRenderer<T> renderer) {
        registry.register(type, Lists.newArrayList(), helper, renderer);
    }

}
