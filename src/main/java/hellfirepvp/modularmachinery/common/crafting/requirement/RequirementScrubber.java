// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.util.Asyncable;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentScrubber;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeScrubber;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentScrubberProvider;
import hellfirepvp.modularmachinery.common.tiles.TileScrubberProvider;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;
import hellfirepvp.modularmachinery.common.integration.nuclearcraft.IRequirementRadiation;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentRadiation;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Radiation;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ResultChance;

import javax.annotation.Nonnull;
import java.util.List;

public class RequirementScrubber extends ComponentRequirement.PerTick<Radiation, RequirementTypeScrubber> implements IMultiChunkRequirement, IRequirementRadiation, Asyncable {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final int chunkRange;
    private final double amount;
    private final double minPerChunk;
    private final double maxPerChunk;

    public static RequirementScrubber from(int chunkRange) {
        requirementValidator.validateNotNegative(chunkRange, "Chunk range must be a positive number!");

        return new RequirementScrubber(IOType.INPUT, chunkRange, Double.MAX_VALUE, 0, Double.MAX_VALUE);
    }

    private RequirementScrubber(IOType actionType, int chunkRange, double amount, double minPerChunk, double maxPerChunk) {
        super((RequirementTypeScrubber) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_SCRUBBER), actionType);
        this.chunkRange = chunkRange;
        this.amount = amount;
        this.minPerChunk = minPerChunk;
        this.maxPerChunk = maxPerChunk;
    }

    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> processingComponent, RecipeCraftingContext recipeCraftingContext) {
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
            getRadiationHandler(processingComponent).activate(recipeCraftingContext, chunkRange);
            return CraftCheck.success();
        });
    }

    @Nonnull
    @Override
    public CraftCheck finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
            getRadiationHandler(component).deactivate(context);
            return true;
        });
        return super.finishCrafting(component, context, chance);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> processingComponent, RecipeCraftingContext recipeCraftingContext) {
        MachineComponent<?> cmp = processingComponent.getComponent();
        return cmp.getComponentType() instanceof ComponentScrubber &&
                cmp instanceof MachineComponentScrubberProvider &&
                cmp.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Radiation, RequirementTypeScrubber> deepCopy() {
        return RequirementScrubber.from(chunkRange);
    }

    @Override
    public ComponentRequirement<Radiation, RequirementTypeScrubber> deepCopyModified(List<RecipeModifier> list) {
        return deepCopy();
    }

    private TileScrubberProvider getRadiationHandler(ProcessingComponent<?> component) {
        return (TileScrubberProvider) component.getComponent().getContainerProvider();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.scrubber";
    }

    @Override
    public JEIComponent<Radiation> provideJEIComponent() {
        return new JEIComponentRadiation(this, true);
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
}
