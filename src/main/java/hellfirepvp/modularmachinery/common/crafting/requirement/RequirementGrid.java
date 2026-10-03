package hellfirepvp.modularmachinery.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentGrid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.ModularMagicRequirements;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeGrid;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentGrid;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Grid;
import hellfirepvp.modularmachinery.common.tiles.TileGridProvider;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentGridProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementGrid extends ComponentRequirement.PerTick<Grid, RequirementTypeGrid> implements Asyncable {

    public float power;
    private boolean tickSatisfied;

    public RequirementGrid(IOType actionType, float power) {
        super((RequirementTypeGrid) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_GRID), actionType);
        this.power = power;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentGrid &&
            cmp instanceof MachineComponentGridProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context) {
        TileGridProvider provider = (TileGridProvider) component.getComponent().getContainerProvider();
        CraftCheck result = ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
            CraftCheck check = checkProvider(provider);
            provider.setPower(check.isSuccess() ? (actionType == IOType.OUTPUT ? -power : power) : 0);
            return check;
        });
        tickSatisfied |= result.isSuccess();
        return result;
    }

    @Override
    public void startIOTick(RecipeCraftingContext context, float durationMultiplier) {
        tickSatisfied = false;
    }

    @Nonnull
    @Override
    public CraftCheck resetIOTick(RecipeCraftingContext context) {
        CraftCheck result = tickSatisfied ? CraftCheck.success()
            : CraftCheck.failure("error.modularmachinery.requirement.grid.less");
        tickSatisfied = false;
        return result;
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return canStartCrafting(component, context, Lists.newArrayList()).isSuccess();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        TileGridProvider provider = (TileGridProvider) component.getComponent().getContainerProvider();
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> checkProvider(provider));
    }

    private CraftCheck checkProvider(TileGridProvider provider) {
        // 已登记的本仓负载不应作为新增负载重复计算；总发电量仍须满足本配方。
        float created = provider.getFreq().getPowerCreated();
        float available = created - provider.getFreq().getPowerDrain() + Math.max(0, provider.getPower());
        if (actionType == IOType.INPUT && Math.min(created, available) < power) {
            return CraftCheck.failure("error.modularmachinery.requirement.grid.less");
        } else {
            return CraftCheck.success();
        }
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementGrid deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementGrid deepCopyModified(List<RecipeModifier> list) {
        float power = RecipeModifier.applyModifiers(list, this, this.power, false);
        return new RequirementGrid(actionType, power);
    }

    @Override
    public JEIComponentGrid provideJEIComponent() {
        return new JEIComponentGrid(this);
    }
}
