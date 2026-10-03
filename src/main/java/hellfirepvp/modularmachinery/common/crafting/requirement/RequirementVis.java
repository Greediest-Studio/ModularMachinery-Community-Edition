// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import java.util.List;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentVis;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeVis;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentVisProvider;
import hellfirepvp.modularmachinery.common.tiles.TileVisProvider;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentVis;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;

public class RequirementVis extends AbstractMultiComponentRequirement<Vis, RequirementTypeVis> implements IMultiChunkRequirement {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final Vis vis;

    public static RequirementVis from(IOType ioType, int chunkRange, float amount, float minPerChunk, float maxPerChunk) {
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");
        requirementValidator.validateNotNegative(amount, "Amount must be a positive number!");
        if (ioType == IOType.OUTPUT) {requirementValidator.validateNotAbove(amount, TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK, String.format("Amount cannot be greater than maximum amount allowed in chunk (this is a TC API limitation): %f", TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));}

        requirementValidator.validateNotNegative(minPerChunk, "Minimum per chunk must be a positive number!");
        requirementValidator.validateNotAbove(maxPerChunk, TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK, String.format("Amount cannot be greater than maximum amount allowed in chunk (this is a TC API limitation): %f", TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));

        return new RequirementVis(ioType, chunkRange, amount, minPerChunk, maxPerChunk);
    }

    public static RequirementVis from(IOType ioType, int chunkRange, float amount) {
        return from(ioType, chunkRange, amount, 0, TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK);
    }

    private RequirementVis(IOType actionType, int chunkRange, float amount, float minPerChunk, float maxPerChunk) {
        super((RequirementTypeVis) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_VIS), actionType);
        this.vis = new Vis(amount, chunkRange, minPerChunk, maxPerChunk);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentVis &&
                cmp instanceof MachineComponentVisProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Vis, RequirementTypeVis> deepCopy() {
        return new RequirementVis(this.actionType, this.vis.getChunkRange(), this.vis.getAmount(), this.vis.getMinPerChunk(), this.vis.getMaxPerChunk());
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.vis";
    }

    @Override
    public JEIComponent<Vis> provideJEIComponent() {
        return new JEIComponentVis(this.vis);
    }

    @Override
    public IOType getIOType() {
        return actionType;
    }

    @Override
    public int getChunkRange() {
        return this.vis.getChunkRange();
    }

    @Override
    public double getAmount() {
        return this.vis.getAmount();
    }

    @Override
    public double getMinPerChunk() {
        return vis.getMinPerChunk();
    }

    @Override
    public double getMaxPerChunk() {
        return vis.getMaxPerChunk();
    }

    @Override
    public ComponentRequirement<Vis, RequirementTypeVis> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementVis(actionType, vis.getChunkRange(), (float) modifiedAmount(modifiers, vis.getAmount()), vis.getMinPerChunk(), vis.getMaxPerChunk());
    }

}
