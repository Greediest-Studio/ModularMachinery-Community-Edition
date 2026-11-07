package github.kasuminova.mmce.common.concurrent;

import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.util.ResourceLocation;

public class FactoryRecipeSearchTask extends RecipeSearchTask {
    private final FactoryRecipeThread                  thread;
    private final Object2IntArrayMap<ResourceLocation> runningRecipes = new Object2IntArrayMap<>();
    // MMCE Complement 的 Mixin 通过 @Shadow 访问此字段，保留名称和类型以兼容附属模组。
    private final TileFactoryController                factory;

    public FactoryRecipeSearchTask(
        TileFactoryController controller,
        DynamicMachine currentMachine,
        int maxParallelism,
        Iterable<MachineRecipe> recipeList,
        FactoryRecipeThread thread,
        ActiveMachineRecipe[] running) {
        super(controller, currentMachine, maxParallelism, recipeList, thread);
        this.factory = controller;

        for (ActiveMachineRecipe recipe : running) {
            ResourceLocation registryName = recipe.getRecipe().getRegistryName();
            int prevCount = runningRecipes.getInt(registryName);
            runningRecipes.put(registryName, prevCount + 1);
        }

        this.thread = thread;
    }

    @Override
    protected boolean canCheck(MachineRecipe recipe) {
        // If the recipe specifies a thread name, determine if the thread name matches.
        String recipeRequiredName = recipe.getThreadName();
        if (!recipeRequiredName.isEmpty() && (thread == null || !thread.getThreadName().equals(recipeRequiredName))) {
            return false;
        }
        // If the number of running identical recipes in the factory exceeds
        // the maximum number defined by the recipe, then this recipe is not checked.
        int maxThreads = recipe.getMaxThreads();
        if (maxThreads != -1 && runningRecipes.getInt(recipe.getRegistryName()) >= maxThreads) {
            return false;
        }
        return super.canCheck(recipe);
    }
}
