// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentWillMultiChunk;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeWillMultiChunk;
import hellfirepvp.modularmachinery.common.tiles.TileWillMultiChunkProvider;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentWillMultiChunk;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.DemonWill;

import javax.annotation.Nonnull;

public class RequirementWillMultiChunk extends AbstractMultiComponentRequirement<DemonWill, RequirementTypeWillMultiChunk> implements IMultiChunkRequirement {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    public EnumDemonWillType willType;
    private final int chunkRange;
    private final double amount;
    private final double minPerChunk;
    private final double maxPerChunk;

    public static RequirementWillMultiChunk from(IOType ioType, int chunkRange, double amount, double minPerChunk, double maxPerChunk, String willType) {
        requirementValidator.validateNotNegative(amount, "Amount must be a positive number!");
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");
        requirementValidator.validateNotNegative(minPerChunk, "Minimum per chunk must be a positive number!");
        requirementValidator.validateNotNegative(maxPerChunk, "Max per chunk must be a positive number!");

        EnumDemonWillType will;
        try {
            will = EnumDemonWillType.valueOf(willType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RequirementPrerequisiteFailedException(e.getMessage());
        }
        return new RequirementWillMultiChunk(ioType, chunkRange, amount, minPerChunk, maxPerChunk, will);
    }

    private RequirementWillMultiChunk(IOType actionType, int chunkRange, double amount, double minPerChunk, double maxPerChunk, EnumDemonWillType willType) {
        super((RequirementTypeWillMultiChunk) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_WILL_MULTI_CHUNK), actionType);
        this.chunkRange = chunkRange;
        this.amount = amount;
        this.minPerChunk = minPerChunk;
        this.maxPerChunk = maxPerChunk;
        this.willType = willType;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return cpn.getContainerProvider() instanceof TileWillMultiChunkProvider &&
                cpn.getComponentType() instanceof ComponentWillMultiChunk &&
                cpn.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<DemonWill, RequirementTypeWillMultiChunk> deepCopy() {
        return new RequirementWillMultiChunk(this.actionType, this.chunkRange, this.amount, this.minPerChunk, this.maxPerChunk, this.willType);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.will";
    }

    @Override
    public JEIComponent<DemonWill> provideJEIComponent() {
        return new JEIComponentWillMultiChunk(this);
    }

    @Override
    public IOType getIOType() {
        return actionType;
    }

    @Override
    public int getChunkRange() {
        return this.chunkRange;
    }

    @Override
    public double getAmount() {
        return this.amount;
    }

    @Override
    public double getMinPerChunk() {
        return this.minPerChunk;
    }

    @Override
    public double getMaxPerChunk() {
        return this.maxPerChunk;
    }

    @Override
    public ComponentRequirement<DemonWill, RequirementTypeWillMultiChunk> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementWillMultiChunk(actionType, chunkRange, modifiedAmount(modifiers, amount), minPerChunk, maxPerChunk, willType);
    }

}
