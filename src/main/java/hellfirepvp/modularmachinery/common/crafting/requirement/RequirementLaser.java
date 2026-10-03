// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeLaser;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentLaserProvider;

import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentLaser;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Laser;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementLaser extends AbstractMultiComponentRequirement<Laser, RequirementTypeLaser> {

    private static final RequirementValidator REQUIREMENT_VALIDATOR = RequirementValidator.getInstance();

    private final Laser laser;

    public RequirementLaser(IOType actionType, Laser laser) {
        super((RequirementTypeLaser) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_LASER), actionType);
        this.laser = laser;
    }

    public static RequirementLaser from(IOType ioType, double power) {
        REQUIREMENT_VALIDATOR.validateNotNegative(power, "power");
        return new RequirementLaser(ioType, new Laser(power));
    }

    public double getPower() {
        return laser.power();
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentType &&
                cmp instanceof MachineComponentLaserProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Laser, RequirementTypeLaser> deepCopy() {
        return new RequirementLaser(this.actionType, new Laser(this.laser.power()));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.laser";
    }

    @Override
    public JEIComponent<Laser> provideJEIComponent() {
        return new JEIComponentLaser(laser);
    }

    @Override
    public ComponentRequirement<Laser, RequirementTypeLaser> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementLaser(actionType, new Laser(modifiedAmount(modifiers, laser.power())));
    }

}
