// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentDragonBreath;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeDragonBreath;
import hellfirepvp.modularmachinery.common.integration.iceandfire.DragonType;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentDragonBreathProvider;

import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentDragonBreath;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DragonBreath;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementDragonBreath extends AbstractMultiComponentRequirement<DragonBreath, RequirementTypeDragonBreath> {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final DragonBreath dragonBreath;

    public RequirementDragonBreath(IOType actionType, DragonBreath dragonBreath) {
        super((RequirementTypeDragonBreath) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_DRAGON_BREATH), actionType);
        this.dragonBreath = dragonBreath;
    }

    public static RequirementDragonBreath from(IOType ioType, String type, int amount) {
        requirementValidator.validateNotNegative(amount, "Amount cannot be negative");

        DragonType dragonType;
        try {
            dragonType = DragonType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RequirementPrerequisiteFailedException("Invalid dragon type: " + type);
        }

        return new RequirementDragonBreath(ioType, new DragonBreath(dragonType, amount));
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext recipeCraftingContext) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentDragonBreath &&
                cmp instanceof MachineComponentDragonBreathProvider &&
                cmp.ioType == getActionType();
    }

    public int getAmount() {
        return dragonBreath.getAmount();
    }

    public DragonType getType() {
        return dragonBreath.getDragonType();
    }

    @Override
    public ComponentRequirement<DragonBreath, RequirementTypeDragonBreath> deepCopy() {
        return new RequirementDragonBreath(actionType, new DragonBreath(dragonBreath.getDragonType(), dragonBreath.getAmount()));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.dragon";
    }

    @Override
    public JEIComponent<DragonBreath> provideJEIComponent() {
        return new JEIComponentDragonBreath(dragonBreath);
    }

    @Override
    public ComponentRequirement<DragonBreath, RequirementTypeDragonBreath> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementDragonBreath(actionType, new DragonBreath(dragonBreath.getDragonType(), (int) Math.ceil(modifiedAmount(modifiers, dragonBreath.getAmount()))));
    }

}
