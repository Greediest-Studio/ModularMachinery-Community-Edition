// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentFlux;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFlux;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentFluxProvider;
import hellfirepvp.modularmachinery.common.tiles.TileFluxProvider;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentFlux;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Flux;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementFlux extends AbstractMultiComponentRequirement<Flux, RequirementTypeFlux> implements IMultiChunkRequirement {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final int chunkRange;
    private final double amount;
    private final double minPerChunk;
    private final double maxPerChunk;

    public static RequirementFlux from(IOType ioType, int chunkRange, float amount) {
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");
        requirementValidator.validateNotNegative(amount, "Amount must be a positive number!");
        if (ioType == IOType.OUTPUT) {requirementValidator.validateNotAbove(amount, TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK, String.format("Amount cannot be greater than maximum amount allowed in chunk (this is a TC API limitation): %f", TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));}

        return new RequirementFlux(ioType, chunkRange, amount, 0, TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK);
    }

    public static RequirementFlux from(IOType ioType, int chunkRange, double amount, double minPerChunk, double maxPerChunk) {
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");
        requirementValidator.validateNotNegative(amount, "Amount must be a positive number!");
        if (ioType == IOType.OUTPUT) {requirementValidator.validateNotAbove(amount, TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK, String.format("Amount cannot be greater than maximum amount allowed in chunk (this is a TC API limitation): %f", TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));}
        requirementValidator.validateNotNegative(minPerChunk, "Minimum per chunk must be a positive number!");
        requirementValidator.validateNotAbove(maxPerChunk, TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK, String.format("Max per chunk cannot be greater than maximum amount allowed in chunk (this is a TC API limitation): %f", TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));

        return new RequirementFlux(ioType, chunkRange, amount, minPerChunk, maxPerChunk);
    }

    public RequirementFlux(IOType actionType, int chunkRange, double amount, double minPerChunk, double maxPerChunk) {
        super((RequirementTypeFlux) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_FLUX), actionType);
        this.chunkRange = chunkRange;
        this.amount = amount;
        this.minPerChunk = minPerChunk;
        this.maxPerChunk = maxPerChunk;
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
        return minPerChunk;
    }

    @Override
    public double getMaxPerChunk() {
        return maxPerChunk;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentFlux &&
                cmp instanceof MachineComponentFluxProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Flux, RequirementTypeFlux> deepCopy() {
        return new RequirementFlux(getActionType(), chunkRange, amount, minPerChunk, maxPerChunk);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.flux";
    }

    @Override
    public JEIComponent<Flux> provideJEIComponent() {
        return new JEIComponentFlux(new Flux((float) this.amount, chunkRange));
    }

    @Override
    public ComponentRequirement<Flux, RequirementTypeFlux> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementFlux(actionType, chunkRange, modifiedAmount(modifiers, amount), minPerChunk, maxPerChunk);
    }

}
