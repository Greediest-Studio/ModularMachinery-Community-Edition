// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeHeat;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentHeatProvider;

import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentHeat;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Heat;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementHeat extends AbstractMultiComponentRequirement<Heat, RequirementTypeHeat> {

    private static final RequirementValidator REQUIREMENT_VALIDATOR = RequirementValidator.getInstance();

    private final Heat heat;

    public RequirementHeat(IOType actionType, Heat heat) {
        super((RequirementTypeHeat) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_HEAT), actionType);
        this.heat = heat;
    }

    public static RequirementHeat from(IOType actionType, double heat) {
        REQUIREMENT_VALIDATOR.validateNotNegative(heat, "heat");
        return new RequirementHeat(actionType, new Heat(heat));
    }

    public double getTemperature() {
        return heat.temperature();
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentType &&
                cmp instanceof MachineComponentHeatProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Heat, RequirementTypeHeat> deepCopy() {
        return new RequirementHeat(this.getActionType(), new Heat(heat.temperature()));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.heat";
    }

    @Override
    public JEIComponent<Heat> provideJEIComponent() {
        return new JEIComponentHeat(heat);
    }

    @Override
    public ComponentRequirement<Heat, RequirementTypeHeat> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementHeat(actionType, new Heat(modifiedAmount(modifiers, heat.temperature())));
    }

}
