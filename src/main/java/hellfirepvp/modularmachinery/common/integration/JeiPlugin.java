package hellfirepvp.modularmachinery.common.integration;

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
        if (Mods.TC6.isPresent() && !Mods.TAHUMIC_JEI.isPresent()) {
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
    }
}
