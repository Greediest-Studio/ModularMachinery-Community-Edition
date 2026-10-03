package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentCatalyst;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementCatalyst extends RequirementIngredientArray {
    protected final List<RecipeModifier> modifierList = new ArrayList<>();
    protected final List<String>         toolTipList  = new ArrayList<>();
    protected       boolean              isRequired   = false;
    protected       boolean              consumePerParallel = true;

    public RequirementCatalyst(ItemStack item) {
        this(Collections.singletonList(new ChancedIngredientStack(item)));
    }

    public RequirementCatalyst(String oreDictName, int amount) {
        this(Collections.singletonList(new ChancedIngredientStack(oreDictName, amount)));
    }

    public RequirementCatalyst(List<ChancedIngredientStack> ingredients) {
        super(RequirementTypesMM.REQUIREMENT_CATALYST != null ? RequirementTypesMM.REQUIREMENT_CATALYST : RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY,
              ingredients, IOType.INPUT);
        setParallelizeUnaffected(true);
    }

    public void addModifier(RecipeModifier modifier) {
        modifierList.add(modifier);
    }

    public void addTooltip(String tooltip) {
        toolTipList.add(tooltip);
    }

    public List<String> getToolTipList() {
        return toolTipList;
    }

    public boolean isConsumePerParallel() {
        return consumePerParallel;
    }

    public void setConsumePerParallel(boolean consumePerParallel) {
        this.consumePerParallel = consumePerParallel;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean isActive() {
        return isRequired;
    }

    public void setActive(boolean active) {
        this.isRequired = active;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public void reset() {
        this.isRequired = false;
    }

    @Override
    public void resetForGroupCheck() {
        reset();
    }

    @Override
    public void setParallelism(int parallelism) {
        if (consumePerParallel) {
            this.parallelism = parallelism;
        } else {
            this.parallelism = 1;
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context) {
        // Test consumption on a snapshot to prevent polluting copied components if insufficient
        List<ProcessingComponent<?>> testCopied = ItemUtils.copyItemHandlerComponents(components);
        if (super.canStartCrafting(testCopied, context).isSuccess()) {
            super.canStartCrafting(components, context);
            addModifierToContext(context);
            this.isRequired = true;
            return CraftCheck.success();
        } else {
            this.isRequired = false;
            return CraftCheck.skipComponent();
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        return maxParallelism;
    }

    protected void addModifierToContext(final RecipeCraftingContext context) {
        for (RecipeModifier modifier : modifierList) {
            context.addPermanentModifier(modifier);
        }
    }

    /** 恢复本次已启用的效果；只有开工索引尚未经过本需求时才补扣催化剂。 */
    public void restoreAfterLoad(RecipeCraftingContext context, boolean pendingConsumption) {
        addModifierToContext(context);
        isRequired = pendingConsumption;
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        startCraftingChecked(components, context, chance);
    }

    @Override
    public boolean startCraftingChecked(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (!isRequired) return true;
        if (!super.startCraftingChecked(components, context, chance)) return false;
        isRequired = false;
        return true;
    }

    @Override
    public RequirementCatalyst deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementCatalyst deepCopyModified(List<RecipeModifier> modifiers) {
        ArrayList<ChancedIngredientStack> copiedIngredients = new ArrayList<>();

        ingredients.forEach(item -> {
            ChancedIngredientStack copied = item.copy();

            switch (copied.ingredientType) {
                case ITEMSTACK -> {
                    ItemStack itemStack = copied.itemStack;
                    int amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, itemStack.getCount(), false));
                    if (amt == itemStack.getCount() && RequirementTypesMM.REQUIREMENT_CATALYST != null) {
                        amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_CATALYST, actionType, itemStack.getCount(), false));
                    }
                    itemStack.setCount(amt);
                }
                case ORE_DICT -> {
                    int amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.count, false));
                    if (amt == item.count && RequirementTypesMM.REQUIREMENT_CATALYST != null) {
                        amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_CATALYST, actionType, item.count, false));
                    }
                    copied.count = amt;
                }
            }
            float ch = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.chance, true);
            if (ch == item.chance && RequirementTypesMM.REQUIREMENT_CATALYST != null) {
                ch = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_CATALYST, actionType, item.chance, true);
            }
            copied.chance = ch;

            copiedIngredients.add(copied);
        });

        RequirementCatalyst catalyst = new RequirementCatalyst(copiedIngredients);
        catalyst.modifierList.addAll(this.modifierList);
        catalyst.toolTipList.addAll(toolTipList);
        catalyst.consumePerParallel = this.consumePerParallel;
        catalyst.isRequired = this.isRequired;
        float ch = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, chance, true);
        if (ch == chance && RequirementTypesMM.REQUIREMENT_CATALYST != null) {
            ch = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_CATALYST, actionType, chance, true);
        }
        catalyst.chance = ch;
        return catalyst;
    }

    @Override
    public RequirementCatalyst postDeepCopy(ComponentRequirement<?, ?> another) {
        super.postDeepCopy(another);
        if (another instanceof RequirementCatalyst catalyst) {
            this.consumePerParallel = catalyst.consumePerParallel;
            this.isRequired = catalyst.isRequired;
        }
        return this;
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return new JEIComponentCatalyst(this);
    }
}

