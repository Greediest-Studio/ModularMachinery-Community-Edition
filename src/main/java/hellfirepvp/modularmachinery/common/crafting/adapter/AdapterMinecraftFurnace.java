/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 * <p>
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.recipe.RecipeCheckEvent;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: AdapterMinecraftFurnace
 * Created by HellFirePvP
 * Date: 23.07.2017 / 14:48
 */
public class AdapterMinecraftFurnace extends RecipeAdapter {

    public AdapterMinecraftFurnace() {
        super(new ResourceLocation("minecraft", "furnace"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
        Map<ItemStack, ItemStack> inputOutputMap = furnaceRecipes.getSmeltingList();

        List<MachineRecipe> smeltingRecipes = new ArrayList<>(inputOutputMap.size());
        for (Map.Entry<ItemStack, ItemStack> smelting : inputOutputMap.entrySet()) {
            ItemStack input = smelting.getKey();
            ItemStack output = smelting.getValue();
            if (input.isEmpty() || output.isEmpty()) {
                continue;
            }

            for (ItemStack resolvedInput : resolveSmeltingInputs(furnaceRecipes, inputOutputMap.keySet(), input, output)) {
                MachineRecipe recipe = createSmeltingRecipe(
                    owningMachineName,
                    modifiers,
                    additionalRequirements,
                    eventHandlers,
                    recipeTooltips,
                    resolvedInput,
                    output
                );
                smeltingRecipes.add(recipe);
            }
        }
        incId++;
        return smeltingRecipes;
    }

    private MachineRecipe createSmeltingRecipe(final ResourceLocation owningMachineName,
                                               final List<RecipeModifier> modifiers,
                                               final List<ComponentRequirement<?, ?>> additionalRequirements,
                                               final Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                               final List<String> recipeTooltips,
                                               final ItemStack input,
                                               final ItemStack output) {
        int tickTime = Math.round(Math.max(1, RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_DURATION, null, 120, false)));
        float experience = FurnaceRecipes.instance().getSmeltingExperience(output);

        MachineRecipe recipe = createRecipeShell(
            new ResourceLocation("minecraft", "smelting_" + incId + "_" + input + "_" + output),
            owningMachineName,
            tickTime, 0, false);
        recipe.addRecipeEventHandler(RecipeCheckEvent.class, (IEventHandler<RecipeCheckEvent>) event -> {
            if (event.phase == Phase.START) {
                event.getActiveRecipe().getDataCompound().setFloat("experience", experience);
            }
        });

        int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false));
        if (inAmount > 0) {
            recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, inAmount)));
        }

        int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, output.getCount(), false));
        if (outAmount > 0) {
            recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(output, outAmount)));
        }

        int inEnergy = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, 20, false));
        if (inEnergy > 0) {
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, inEnergy));
        }

        for (ComponentRequirement<?, ?> requirement : additionalRequirements) {
            recipe.addRequirement(requirement.deepCopyModified(modifiers).postDeepCopy(requirement));
        }

        for (Map.Entry<Class<?>, List<IEventHandler<RecipeEvent>>> entry : eventHandlers.entrySet()) {
            for (IEventHandler<RecipeEvent> handler : entry.getValue()) {
                recipe.addRecipeEventHandler(entry.getKey(), handler);
            }
        }

        for (String tooltip : recipeTooltips) {
            recipe.addTooltip(tooltip);
        }

        return recipe;
    }

    private static Collection<ItemStack> resolveSmeltingInputs(final FurnaceRecipes furnaceRecipes,
                                                               final Collection<ItemStack> registeredInputs,
                                                               final ItemStack input,
                                                               final ItemStack output) {
        if (input.getItemDamage() != OreDictionary.WILDCARD_VALUE || input.isItemStackDamageable()) {
            return Collections.singletonList(input);
        }
        if (input.getItem().getCreativeTab() == null) {
            return Collections.emptyList();
        }

        NonNullList<ItemStack> subItems = NonNullList.create();
        input.getItem().getSubItems(input.getItem().getCreativeTab(), subItems);

        List<ItemStack> resolvedInputs = new ArrayList<>(subItems.size());
        for (ItemStack subItem : subItems) {
            if (subItem.isEmpty() || hasExactSmeltingInput(registeredInputs, subItem)) {
                continue;
            }

            ItemStack smeltingResult = furnaceRecipes.getSmeltingResult(subItem);
            if (smeltingResult.isEmpty() || !ItemStack.areItemStacksEqual(smeltingResult, output)) {
                continue;
            }

            ItemStack resolvedInput = ItemUtils.copyStackWithSize(subItem, input.getCount());
            if (input.hasTagCompound()) {
                resolvedInput.setTagCompound(input.getTagCompound().copy());
            }
            resolvedInputs.add(resolvedInput);
        }
        return resolvedInputs;
    }

    private static boolean hasExactSmeltingInput(final Collection<ItemStack> registeredInputs, final ItemStack candidate) {
        for (ItemStack registeredInput : registeredInputs) {
            if (registeredInput.isEmpty() || registeredInput.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                continue;
            }
            if (registeredInput.getItem() == candidate.getItem() && registeredInput.getItemDamage() == candidate.getItemDamage()) {
                return true;
            }
        }
        return false;
    }
}
