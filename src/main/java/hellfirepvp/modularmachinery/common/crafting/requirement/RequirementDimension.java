// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentDimension;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeDimension;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentDimensionProvider;

import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentDimension;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Dimension;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class RequirementDimension extends ComponentRequirement<Dimension, RequirementTypeDimension> {

    private final Dimension dimension;

    public static RequirementDimension from(IOType ioType, int dimensionId) {
        if (ioType.equals(IOType.OUTPUT)) {throw new RequirementPrerequisiteFailedException("Input IOType is not supported");}

        return new RequirementDimension(ioType, new Dimension(dimensionId));
    }

    private RequirementDimension(IOType actionType, Dimension dimension) {
        super((RequirementTypeDimension) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_DIMENSION), actionType);
        this.dimension = dimension;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentDimension &&
                cmp instanceof MachineComponentDimensionProvider &&
                cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        return getDimensionHandler(component).canHandle(this);
    }

    @Override
    public ComponentRequirement<Dimension, RequirementTypeDimension> deepCopy() {
        return new RequirementDimension(getActionType(), new Dimension(this.dimension.getId(), this.dimension.getName()));
    }

    @Override
    public ComponentRequirement<Dimension, RequirementTypeDimension> deepCopyModified(List<RecipeModifier> modifiers) {
        return deepCopy();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.dimension";
    }

    @Override
    public JEIComponent<Dimension> provideJEIComponent() {
        return new JEIComponentDimension(this.dimension);
    }

    public Dimension getDimension() {
        return dimension;
    }

    @SuppressWarnings("unchecked")
    private IRequirementHandler<RequirementDimension> getDimensionHandler(ProcessingComponent<?> component) {
        return (IRequirementHandler<RequirementDimension>) component.getComponent().getContainerProvider();
    }
}
