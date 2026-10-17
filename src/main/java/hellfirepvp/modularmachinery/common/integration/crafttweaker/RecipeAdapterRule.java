package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluidPerTick;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 配方适配器的条件追加规则。多个输入、输出条件之间均为 AND，追加内容复用 RecipePrimer。
 */
@ZenRegister
@ZenClass("mods.modularmachinery.RecipeAdapterRule")
public class RecipeAdapterRule extends RecipePrimer {

    private final RecipeAdapterBuilder adapter;
    private final List<IIngredient> inputs = new ArrayList<>();
    private final List<IIngredient> outputs = new ArrayList<>();

    RecipeAdapterRule(RecipeAdapterBuilder adapter) {
        super(null, adapter.getAssociatedMachineName(), 0, 0, false);
        this.adapter = adapter;
    }

    /**
     * 为当前规则追加输入条件。请在 addInput/addOutput 等返回 RecipePrimer 的方法之前调用。
     */
    @ZenMethod
    public RecipeAdapterRule forInput(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            this.inputs.add(input.amount(1));
        }
        return this;
    }

    /**
     * 为当前规则追加输出条件，可与 forInput 链式组合。
     */
    @ZenMethod
    public RecipeAdapterRule forOutput(IIngredient... outputs) {
        for (IIngredient output : outputs) {
            this.outputs.add(output.amount(1));
        }
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterRule setLoadJEI(boolean load) {
        super.setLoadJEI(load);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterRule setJEICategory(String key) {
        super.setJEICategory(key);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterRule setJEICategory(String key, String displayName) {
        super.setJEICategory(key, displayName);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterRule setJEICategory(String key, IItemStack icon) {
        super.setJEICategory(key, icon);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterRule setJEICategory(String key, String displayName, IItemStack icon) {
        super.setJEICategory(key, displayName, icon);
        return this;
    }

    @Override
    @ZenMethod
    public void build() {
        if (inputs.isEmpty() && outputs.isEmpty()) {
            CraftTweakerAPI.logError("[ModularMachinery] RecipeAdapterRule requires at least one input or output condition.");
            return;
        }
        adapter.addRecipeRule(this);
    }

    boolean matches(MachineRecipe recipe) {
        List<ComponentRequirement<?, ?>> requirements = recipe.getCraftingRequirements();
        return matchesAll(inputs, requirements, IOType.INPUT)
            && matchesAll(outputs, requirements, IOType.OUTPUT);
    }

    private static boolean matchesAll(List<IIngredient> ingredients,
                                      List<ComponentRequirement<?, ?>> requirements,
                                      IOType ioType) {
        for (IIngredient ingredient : ingredients) {
            boolean matched = false;
            for (ComponentRequirement<?, ?> requirement : requirements) {
                if (requirement.getActionType() == ioType && matchesRequirement(ingredient, requirement)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesRequirement(IIngredient ingredient, ComponentRequirement<?, ?> requirement) {
        // 流体条件只匹配流体需求，不把装有该流体的物品容器当成流体输入或输出。
        if (ingredient instanceof ILiquidStack) {
            if (requirement instanceof RequirementFluid fluid) {
                return matchesFluid(ingredient, fluid.required);
            }
            if (requirement instanceof RequirementFluidPerTick fluid) {
                return matchesFluid(ingredient, fluid.required);
            }
            return false;
        }

        if (requirement instanceof RequirementItem item) {
            return switch (item.requirementType) {
                case ITEMSTACKS -> matchesItem(ingredient, item.required, item.tag);
                case OREDICT -> matchesOreDict(ingredient, item.oreDictName, item.tag);
                case FUEL, ITEMSTACK_ARRAY -> false;
            };
        }
        if (requirement instanceof RequirementIngredientArray array) {
            for (ChancedIngredientStack item : array.getIngredients()) {
                boolean matched = switch (item.ingredientType) {
                    case ITEMSTACK -> matchesItem(ingredient, item.itemStack, item.tag);
                    case ORE_DICT -> matchesOreDict(ingredient, item.oreDictName, item.tag);
                };
                if (matched) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesItem(IIngredient ingredient, ItemStack required, @Nullable NBTTagCompound tag) {
        ItemStack stack = required.copy();
        stack.setCount(1);
        if (tag != null) {
            stack.setTagCompound(tag.copy());
        }
        return ingredient.matches(CraftTweakerMC.getIItemStack(stack));
    }

    private static boolean matchesOreDict(IIngredient ingredient, String oreDictName, @Nullable NBTTagCompound tag) {
        for (ItemStack stack : OreDictionary.getOres(oreDictName, false)) {
            if (matchesItem(ingredient, stack, tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesFluid(IIngredient ingredient, FluidStack required) {
        FluidStack stack = required.copy();
        stack.amount = 1;
        return ingredient.matches(CraftTweakerMC.getILiquidStack(stack));
    }
}
