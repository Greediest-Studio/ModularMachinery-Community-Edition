package github.kasuminova.mmce.common.concurrent;

import github.kasuminova.mmce.common.util.concurrent.TimeRecordingTask;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;

public class RecipeSearchTask extends TimeRecordingTask<RecipeCraftingContext> {
    protected final TileMultiblockMachineController controller;
    protected final DynamicMachine                  currentMachine;
    protected final int                             maxParallelism;
    protected final Iterable<MachineRecipe>         recipeList;
    private final   RecipeThread                    thread;
    private final long controllerModeVersion;
    private String controllerModeFailure;
    protected       CraftingStatus                  status = CraftingStatus.IDLE;

    public RecipeSearchTask(TileMultiblockMachineController controller, DynamicMachine currentMachine, int maxParallelism, Iterable<MachineRecipe> recipeList, final RecipeThread thread) {
        this.controller = controller;
        this.currentMachine = currentMachine;
        this.maxParallelism = maxParallelism;
        this.recipeList = recipeList;
        this.thread = thread;
        this.controllerModeVersion = controller.getControllerModeVersion();
    }

    @Override
    protected RecipeCraftingContext computeTask() {
        DynamicMachine foundMachine = controller.getFoundMachine();
        if (foundMachine == null) {
            return null;
        }

        MachineRecipe highestValidity = null;
        RecipeCraftingContext.CraftingCheckResult highestValidityResult = null;
        float validity = 0F;

        RecipeCraftingContext.beginSearchComponentMatchCache(controller);
        try {
            for (MachineRecipe recipe : recipeList) {
                if (!canCheck(recipe)) {
                    continue;
                }

                ActiveMachineRecipe activeRecipe = new ActiveMachineRecipe(recipe, maxParallelism);
                RecipeCraftingContext context = thread != null ? thread.createContext(activeRecipe) : controller.createContext(activeRecipe);
                RecipeCraftingContext.CraftingCheckResult result = controller.onCheck(context);
                if (result.isSuccess()) {
                    //并发检查
                    foundMachine = controller.getFoundMachine();
                    if (foundMachine == null || !foundMachine.equals(currentMachine)) {
                        RecipeCraftingContextPool.returnCtx(context);
                        return null;
                    }
                    return context;
                }

                if (highestValidity == null || result.getValidity() > validity) {
                    highestValidity = recipe;
                    highestValidityResult = result;
                    validity = result.getValidity();
                }

                RecipeCraftingContextPool.returnCtx(context);
            }
        } finally {
            RecipeCraftingContext.endSearchComponentMatchCache();
        }

        if (highestValidity != null) {
            status = CraftingStatus.failure(
                highestValidityResult.getFirstErrorMessage(""));
        } else if (controllerModeFailure != null) {
            status = CraftingStatus.failure(controllerModeFailure);
        } else {
            status = CraftingStatus.failure(
                TileMultiblockMachineController.Type.NO_RECIPE.getUnlocalizedDescription());
        }

        return null;
    }

    protected boolean canCheck(MachineRecipe recipe) {
        CraftCheck result = recipe.checkControllerModes(controller);
        if (result.isSuccess()) {
            return true;
        }
        // 仅在没有当前模式的候选配方时，才用模式错误作为最终提示。
        if (controllerModeFailure == null) {
            controllerModeFailure = result.getUnlocalizedMessage();
        }
        return false;
    }

    public CraftingStatus getStatus() {
        return status;
    }

    public boolean hasMatchingControllerMode() {
        return controllerModeVersion == controller.getControllerModeVersion();
    }

    public DynamicMachine getCurrentMachine() {
        return currentMachine;
    }
}
