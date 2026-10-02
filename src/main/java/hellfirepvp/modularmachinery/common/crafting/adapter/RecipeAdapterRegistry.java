/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeAdapterRegistry
 * Created by HellFirePvP
 * Date: 23.07.2017 / 14:15
 */
public class RecipeAdapterRegistry {

    @Nullable
    public static Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachine,
                                                             ResourceLocation adapterKey,
                                                             List<RecipeModifier> modifiers,
                                                             List<ComponentRequirement<?, ?>> additionalRequirements,
                                                             Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                             List<String> recipeTooltips) {
        RecipeAdapter adapter = RegistriesMM.ADAPTER_REGISTRY.getValue(adapterKey);
        if (adapter == null) {
            return null;
        }
        // 统一追加，适配器只负责转换原配方，避免漏加或重复添加附加条件。
        Collection<MachineRecipe> recipes = adapter.createRecipesFor(owningMachine, modifiers,
            Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
        for (MachineRecipe recipe : recipes) {
            for (ComponentRequirement<?, ?> requirement : additionalRequirements) {
                recipe.addRequirement(requirement.deepCopyModified(modifiers).postDeepCopy(requirement));
            }
            eventHandlers.forEach((type, handlers) -> handlers.forEach(handler -> recipe.addRecipeEventHandler(type, handler)));
            recipeTooltips.forEach(recipe::addTooltip);
        }
        return recipes;
    }

    public static void registerDynamicMachineAdapters() {
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            RegistriesMM.ADAPTER_REGISTRY.register(new DynamicMachineRecipeAdapter(machine.getRegistryName(), machine));
        }
    }

}
