// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentPotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypePotentialEnergy;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentPotentialEnergyProvider;

import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentPotentialEnergy;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.PotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementPotentialEnergy extends AbstractMultiComponentRequirement<PotentialEnergy, RequirementTypePotentialEnergy> {

    private final PotentialEnergy potentialEnergy;

    public RequirementPotentialEnergy(IOType actionType, PotentialEnergy potentialEnergy) {
        super((RequirementTypePotentialEnergy) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_POTENTIAL_ENERGY), actionType);
        this.potentialEnergy = potentialEnergy;
    }

    public static RequirementPotentialEnergy from(IOType ioType, float amount) {
        return new RequirementPotentialEnergy(ioType, new PotentialEnergy(amount));
    }

    public float getAmount() {
        return this.potentialEnergy.getEnergy();
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext recipeCraftingContext) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentPotentialEnergy &&
                cmp instanceof MachineComponentPotentialEnergyProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<PotentialEnergy, RequirementTypePotentialEnergy> deepCopy() {
        return new RequirementPotentialEnergy(this.actionType, new PotentialEnergy(this.potentialEnergy.getEnergy()));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.potential_energy";
    }

    @Override
    public JEIComponent<PotentialEnergy> provideJEIComponent() {
        return new JEIComponentPotentialEnergy(this.potentialEnergy);
    }

    @Override
    public ComponentRequirement<PotentialEnergy, RequirementTypePotentialEnergy> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementPotentialEnergy(actionType, new PotentialEnergy((float) modifiedAmount(modifiers, potentialEnergy.getEnergy())));
    }

}
