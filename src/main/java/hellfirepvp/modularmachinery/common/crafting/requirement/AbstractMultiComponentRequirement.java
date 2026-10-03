// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ResultChance;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiComponentRequirement<T, V extends RequirementType<T, ? extends ComponentRequirement<T, V>>> extends ComponentRequirement.MultiComponentRequirement<T, V>{

    public AbstractMultiComponentRequirement(V requirementType, IOType actionType) {
        super(requirementType, actionType);
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(List<ProcessingComponent<?>> list, RecipeCraftingContext recipeCraftingContext) {
        List<IRequirementHandler<AbstractMultiComponentRequirement<T, V>>> handlers = getHandlers(list);

        CraftCheck recipeCheck = CraftCheck.failure(getMissingComponentErrorMessage(actionType));

        for (IRequirementHandler<AbstractMultiComponentRequirement<T, V>> handler : handlers) {
            CraftCheck checked = handler.canHandle(this);
            if (checked != null) recipeCheck = checked;
            if (recipeCheck != null && recipeCheck.isSuccess()) {
                return recipeCheck;
            }
        }

        return recipeCheck;
    }

    @Override
    public boolean startCraftingChecked(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType != IOType.INPUT) return true;
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> handle(components));
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        startCraftingChecked(components, context, chance);
    }

    @Override
    public void finishCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType == IOType.OUTPUT) {
            ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> handle(components));
        }
    }

    @Override
    public ComponentRequirement<T, V> deepCopyModified(List<RecipeModifier> list) {
        return deepCopy();
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(List<ProcessingComponent<?>> toCopy) {
        List<ProcessingComponent<?>> copy = new ArrayList<>(toCopy.size());

        toCopy.forEach(component -> {
            ProcessingComponent<?> cmp = new ProcessingComponent<>((MachineComponent<Object>) component.getComponent(), component.getProvidedComponent(), component.getTag());
            copy.add(cmp);
        });

        return copy;
    }

    private boolean handle(List<ProcessingComponent<?>> components) {
        for (IRequirementHandler<AbstractMultiComponentRequirement<T, V>> handler : getHandlers(components)) {
            if (handler.tryHandle(this)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<IRequirementHandler<AbstractMultiComponentRequirement<T, V>>> getHandlers(List<ProcessingComponent<?>> components) {
        List<IRequirementHandler<AbstractMultiComponentRequirement<T, V>>> handlers = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            if (component != null && component.getProvidedComponent() instanceof IRequirementHandler<?> handler) {
                handlers.add((IRequirementHandler<AbstractMultiComponentRequirement<T, V>>) handler);
            }
        }
        return handlers;
    }

    protected double modifiedAmount(List<RecipeModifier> modifiers, double amount) {
        return Math.max(0, RecipeModifier.applyModifiers(modifiers, this, amount, false));
    }

}
