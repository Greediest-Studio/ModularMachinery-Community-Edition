// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentRadiation;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeRadiation;
import hellfirepvp.modularmachinery.common.integration.nuclearcraft.IRequirementRadiation;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentRadiationProvider;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentRadiation;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementRadiation extends AbstractMultiComponentRequirement<Radiation, RequirementTypeRadiation> implements IMultiChunkRequirement, IRequirementRadiation {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final int chunkRange;
    private final double amount;
    private final double minPerChunk;
    private final double maxPerChunk;

    public static RequirementRadiation from(IOType actionType, int chunkRange, double amount) {
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");
        requirementValidator.validateNotNegative(amount, "Amount must be a positive number!");

        return new RequirementRadiation(actionType, chunkRange, amount, 0, Double.MAX_VALUE);
    }

    private RequirementRadiation(IOType actionType, int chunkRange, double amount, double minPerChunk, double maxPerChunk) {
        super((RequirementTypeRadiation) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_RADIATION), actionType);
        this.chunkRange = chunkRange;
        this.amount = amount;
        this.minPerChunk = minPerChunk;
        this.maxPerChunk = maxPerChunk;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentRadiation &&
                cmp instanceof MachineComponentRadiationProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Radiation, RequirementTypeRadiation> deepCopy() {
        return new RequirementRadiation(actionType, chunkRange, amount, minPerChunk, maxPerChunk);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.radiation";
    }

    @Override
    public JEIComponent<Radiation> provideJEIComponent() {
        return new JEIComponentRadiation(this, false);
    }

    @Override
    public IOType getIOType() {
        return actionType;
    }

    @Override
    public int getChunkRange() {
        return chunkRange;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public IOType getType() {
        return actionType;
    }

    @Override
    public double getMinPerChunk() {
        return minPerChunk;
    }

    @Override
    public double getMaxPerChunk() {
        return maxPerChunk;
    }

    @Override
    public ComponentRequirement<Radiation, RequirementTypeRadiation> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementRadiation(actionType, chunkRange, modifiedAmount(modifiers, amount), minPerChunk, maxPerChunk);
    }

}
