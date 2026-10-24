package hellfirepvp.modularmachinery.common.crafting.adapter.enderio;

import crafttweaker.util.IEventHandler;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeOutput;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdapterEnderIOAlloySmelter extends RecipeAdapter {

    public static final int BASE_ENERGY_PER_TICK = 60;

    public AdapterEnderIOAlloySmelter() {
        super(new ResourceLocation("enderio", "alloy_smelter"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        resetIncId();

        List<IManyToOneRecipe> alloyRecipes = AlloyRecipeManager.getInstance().getRecipes();
        List<MachineRecipe> machineRecipes = new ArrayList<>(alloyRecipes.size());

        for (IManyToOneRecipe alloyRecipe : alloyRecipes) {
            // 合成配方与 JEI 展示用的去重配方重复，跳过。
            if (alloyRecipe.isSynthetic() || !alloyRecipe.isValid()) {
                continue;
            }

            MachineRecipe recipe = convertRecipe(alloyRecipe, owningMachineName, modifiers,
                additionalRequirements, eventHandlers, recipeTooltips);
            if (recipe == null) {
                continue;
            }

            machineRecipes.add(recipe);
            incId++;
        }

        return machineRecipes;
    }

    private MachineRecipe convertRecipe(IManyToOneRecipe alloyRecipe,
                                        ResourceLocation owningMachineName,
                                        List<RecipeModifier> modifiers,
                                        List<ComponentRequirement<?, ?>> additionalRequirements,
                                        Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                        List<String> recipeTooltips) {
        int totalEnergy = alloyRecipe.getEnergyRequired();
        if (totalEnergy <= 0) {
            return null;
        }

        int baseTickTime = Math.max(1, Math.round((float) totalEnergy / BASE_ENERGY_PER_TICK));
        int tickTime = Math.max(1, Math.round(RecipeModifier.applyModifiers(
            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, baseTickTime, false)));

        MachineRecipe recipe = createRecipeShell(
            new ResourceLocation("enderio", "alloy_smelter_" + incId),
            owningMachineName, tickTime, incId, false);

        int inputCount = 0;
        for (IRecipeInput input : alloyRecipe.getInputs()) {
            if (input == null || !input.isValid()) {
                continue;
            }
            if (!input.isFluid()) {
                inputCount += addItemInput(recipe, input, modifiers) ? 1 : 0;
            }
        }

        int outputCount = 0;
        for (RecipeOutput output : alloyRecipe.getOutputs()) {
            if (output == null || !output.isValid()) {
                continue;
            }
            if (!output.isFluid()) {
                outputCount += addItemOutput(recipe, output, modifiers) ? 1 : 0;
            }
        }

        // 输入或输出全部无效的配方没有意义，直接跳过。
        if (inputCount == 0 || outputCount == 0) {
            return null;
        }

        // 配置中已经指定了输入能量时不再重复添加，避免 MachineRecipe 拒绝多个能量输入。
        if (!hasEnergyInputRequirement(additionalRequirements)) {
            int energyPerTick = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, BASE_ENERGY_PER_TICK, false));
            if (energyPerTick > 0) {
                recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));
            }
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

    private boolean addItemInput(MachineRecipe recipe, IRecipeInput input, List<RecipeModifier> modifiers) {
        int inAmount = Math.round(RecipeModifier.applyModifiers(
            modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getStackSize(), false));
        if (inAmount <= 0) {
            return false;
        }

        List<ItemStack> alternatives = collectAlternatives(input, inAmount);
        if (alternatives.isEmpty()) {
            return false;
        }

        if (alternatives.size() == 1) {
            ItemStack stack = alternatives.get(0);
            RequirementItem requirement = new RequirementItem(IOType.INPUT, stack);
            if (stack.hasTagCompound()) {
                requirement.tag = stack.getTagCompound().copy();
            }
            recipe.addRequirement(requirement);
            return true;
        }

        List<ChancedIngredientStack> ingredients = new ArrayList<>(alternatives.size());
        for (ItemStack stack : alternatives) {
            ChancedIngredientStack ingredient = new ChancedIngredientStack(stack);
            if (stack.hasTagCompound()) {
                ingredient.tag = stack.getTagCompound().copy();
            }
            ingredients.add(ingredient);
        }
        recipe.addRequirement(new RequirementIngredientArray(ingredients));
        return true;
    }

    private boolean addItemOutput(MachineRecipe recipe, RecipeOutput output, List<RecipeModifier> modifiers) {
        ItemStack stack = output.getOutput();
        if (stack.isEmpty()) {
            return false;
        }
        int outAmount = Math.round(RecipeModifier.applyModifiers(
            modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, stack.getCount(), false));
        if (outAmount <= 0) {
            return false;
        }

        recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(stack, outAmount)));
        return true;
    }

    //展开单个输入槽位可接受的全部材料（矿辞、别名等），并按修改后的数量复制。
    private static List<ItemStack> collectAlternatives(IRecipeInput input, int amount) {
        List<ItemStack> alternatives = new ArrayList<>();
        ItemStack[] equivalents = input.getEquivelentInputs();

        if (equivalents == null || equivalents.length == 0) {
            ItemStack single = input.getInput();
            if (!single.isEmpty()) {
                alternatives.add(ItemUtils.copyStackWithSize(single, amount));
            }
            return alternatives;
        }

        for (ItemStack equivalent : equivalents) {
            if (equivalent == null || equivalent.isEmpty()) {
                continue;
            }
            ItemStack stack = ItemUtils.copyStackWithSize(equivalent, amount);
            if (!containsStack(alternatives, stack)) {
                alternatives.add(stack);
            }
        }
        return alternatives;
    }

    private static boolean containsStack(List<ItemStack> stacks, ItemStack stack) {
        for (ItemStack existing : stacks) {
            if (ItemUtils.matchStacks(existing, stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEnergyInputRequirement(List<ComponentRequirement<?, ?>> requirements) {
        for (ComponentRequirement<?, ?> requirement : requirements) {
            if (requirement instanceof RequirementEnergy && requirement.getActionType() == IOType.INPUT) {
                return true;
            }
        }
        return false;
    }
}
