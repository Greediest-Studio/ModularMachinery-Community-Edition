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
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementCatalyst;
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
 * 物品种类数量条件同样与其他条件为 AND 关系。
 */
@ZenRegister
@ZenClass("mods.modularmachinery.RecipeAdapterRule")
public class RecipeAdapterRule extends RecipePrimer {

    private final RecipeAdapterBuilder adapter;
    private final List<IIngredient> inputs = new ArrayList<>();
    private final List<IIngredient> outputs = new ArrayList<>();

    // 物品种数条件，-1 表示未设置；多次设置时取交集。
    private int minInputItemTypes  = -1;
    private int maxInputItemTypes  = -1;
    private int minOutputItemTypes = -1;
    private int maxOutputItemTypes = -1;

    // 移除规则：命中后该配方不会进入适配器的产出。
    private boolean removal = false;

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

    /**
     * 选择原有输入中物品需求种类数恰好为 count 的配方。
     * 每种物品需求（物品、矿辞、物品组）各计为一种；流体、气体和可选催化剂不计入。
     * 多次调用时条件取交集，可与 forInput/forOutput 链式组合。
     */
    @ZenMethod
    public RecipeAdapterRule forInputItemTypes(int count) {
        return forInputItemTypes(count, count);
    }

    /**
     * 选择原有输入中物品需求种类数在 [min, max] 区间内（含边界）的配方。
     */
    @ZenMethod
    public RecipeAdapterRule forInputItemTypes(int min, int max) {
        if (!checkItemTypeRange(min, max, "forInputItemTypes")) {
            return this;
        }
        this.minInputItemTypes = Math.max(this.minInputItemTypes, min);
        this.maxInputItemTypes = this.maxInputItemTypes < 0 ? max : Math.min(this.maxInputItemTypes, max);
        return this;
    }

    /**
     * 选择原有输出中物品需求种类数恰好为 count 的配方。
     * 每种物品需求（物品、矿辞、物品组）各计为一种；流体、气体和可选催化剂不计入。
     * 多次调用时条件取交集，可与 forInput/forOutput 链式组合。
     */
    @ZenMethod
    public RecipeAdapterRule forOutputItemTypes(int count) {
        return forOutputItemTypes(count, count);
    }

    /**
     * 选择原有输出中物品需求种类数在 [min, max] 区间内（含边界）的配方。
     */
    @ZenMethod
    public RecipeAdapterRule forOutputItemTypes(int min, int max) {
        if (!checkItemTypeRange(min, max, "forOutputItemTypes")) {
            return this;
        }
        this.minOutputItemTypes = Math.max(this.minOutputItemTypes, min);
        this.maxOutputItemTypes = this.maxOutputItemTypes < 0 ? max : Math.min(this.maxOutputItemTypes, max);
        return this;
    }

    private static boolean checkItemTypeRange(int min, int max, String methodName) {
        if (min < 0 || max < 0) {
            CraftTweakerAPI.logError("[ModularMachinery] " + methodName + " requires non-negative item type counts.");
            return false;
        }
        if (min > max) {
            CraftTweakerAPI.logError("[ModularMachinery] " + methodName + " requires min <= max.");
            return false;
        }
        return true;
    }

    /**
     * 将当前规则改为移除规则：命中的配方不会注册，也不会出现在 JEI 中。
     * 必须与筛选条件组合使用，例如 forInputItemTypes(1).remove()；
     * 移除优先于追加，命中后不会应用全局追加或其他规则。
     */
    @ZenMethod
    public RecipeAdapterRule remove() {
        this.removal = true;
        return this;
    }

    boolean isRemoval() {
        return removal;
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
        if (!hasConditions()) {
            CraftTweakerAPI.logError("[ModularMachinery] RecipeAdapterRule requires at least one input, output or item type count condition"
                + (removal ? "; remove() must be combined with a filter condition." : "."));
            return;
        }
        adapter.addRecipeRule(this);
    }

    private boolean hasConditions() {
        // remove() 单独使用会移除适配器的全部配方，因此必须搭配至少一个筛选条件。
        return !inputs.isEmpty() || !outputs.isEmpty()
            || minInputItemTypes >= 0 || maxInputItemTypes >= 0
            || minOutputItemTypes >= 0 || maxOutputItemTypes >= 0;
    }

    boolean matches(MachineRecipe recipe) {
        List<ComponentRequirement<?, ?>> requirements = recipe.getCraftingRequirements();
        return matchesAll(inputs, requirements, IOType.INPUT)
            && matchesAll(outputs, requirements, IOType.OUTPUT)
            && matchesItemTypeRange(countItemTypes(requirements, IOType.INPUT), minInputItemTypes, maxInputItemTypes)
            && matchesItemTypeRange(countItemTypes(requirements, IOType.OUTPUT), minOutputItemTypes, maxOutputItemTypes);
    }

    private static boolean matchesItemTypeRange(int count, int min, int max) {
        if (min < 0 && max < 0) {
            return true;
        }
        if (min >= 0 && count < min) {
            return false;
        }
        return max < 0 || count <= max;
    }

    /**
     * 统计某一方向上物品需求的种类数。
     * 每个物品、矿辞或物品组需求各计一种，物品组内部的可选材料不重复计数；
     * 燃料、流体、气体、能量不计入；可选催化剂作为辅助输入不计入。
     */
    private static int countItemTypes(List<ComponentRequirement<?, ?>> requirements, IOType ioType) {
        int count = 0;
        for (ComponentRequirement<?, ?> requirement : requirements) {
            if (requirement.getActionType() != ioType) {
                continue;
            }
            // 催化剂是可选辅助输入，不属于配方的常规物品输入。
            if (requirement instanceof RequirementCatalyst) {
                continue;
            }
            if (requirement instanceof RequirementItem item) {
                // 燃料需求不指定具体物品，不算作一种物品。
                if (item.requirementType == RequirementItem.ItemRequirementType.ITEMSTACKS
                    || item.requirementType == RequirementItem.ItemRequirementType.OREDICT) {
                    count++;
                }
            } else if (requirement instanceof RequirementIngredientArray) {
                count++;
            }
        }
        return count;
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
