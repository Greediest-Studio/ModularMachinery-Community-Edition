package hellfirepvp.modularmachinery.common.crafting.requirement;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
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
import hellfirepvp.modularmachinery.common.crafting.component.ComponentWill;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.ModularMagicRequirements;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeWill;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentWill;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DemonWill;
import hellfirepvp.modularmachinery.common.tiles.TileWillProvider;

import javax.annotation.Nonnull;
import java.util.List;

public class RequirementWill extends ComponentRequirement<DemonWill, RequirementTypeWill> implements Asyncable {

    public double            willAmount;
    public EnumDemonWillType willType;
    public double            min;
    public double            max;

    public RequirementWill(IOType actionType, double willRequired, EnumDemonWillType willType, double min, double max) {
        super((RequirementTypeWill) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_WILL), actionType);
        this.willAmount = willRequired;
        this.willType = willType;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return cpn.getContainerProvider() instanceof TileWillProvider &&
            cpn.getComponentType() instanceof ComponentWill &&
            cpn.ioType == getActionType();
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        TileWillProvider provider = (TileWillProvider) component.getComponent().getContainerProvider();
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> getActionType() == IOType.INPUT
            ? provider.tryRemoveWill(willAmount, willType, min)
            : checkProvider(provider).isSuccess());
    }

    @Nonnull
    @Override
    public CraftCheck finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        if (getActionType() == IOType.OUTPUT) {
            TileWillProvider willProvider = (TileWillProvider) component.getComponent().getContainerProvider();
            return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
                CraftCheck check = checkProvider(willProvider);
                if (check.isSuccess()) {
                    willProvider.addWill(willAmount, willType);
                }
                return check;
            });
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        TileWillProvider willProvider = (TileWillProvider) component.getComponent().getContainerProvider();
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> checkProvider(willProvider));
    }

    private CraftCheck checkProvider(TileWillProvider willProvider) {
        switch (getActionType()) {
            case INPUT -> {
                if (willAmount < 0 || willProvider.getWill(this.willType) - this.willAmount < Math.max(0, this.min)) {
                    return CraftCheck.failure("error.modularmachinery.requirement.will.less");
                }
            }
            case OUTPUT -> {
                if (willAmount < 0 || willProvider.getWill(this.willType) + this.willAmount > this.max) {
                    return CraftCheck.failure("error.modularmachinery.requirement.will.more");
                }
            }
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementWill deepCopy() {
        return new RequirementWill(actionType, willAmount, willType, min, max);
    }

    @Override
    public RequirementWill deepCopyModified(List<RecipeModifier> list) {
        return new RequirementWill(actionType,
            RecipeModifier.applyModifiers(list, this, willAmount, false), willType, min, max);
    }

    @Override
    public JEIComponentWill provideJEIComponent() {
        return new JEIComponentWill(this);
    }
}
