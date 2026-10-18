package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeAdapterBuilder")
public class RecipeAdapterBuilder extends RecipePrimer {
    protected final List<RecipeModifier> modifiers = new LinkedList<>();
    protected final ResourceLocation     parentMachineName;

    private final List<RecipeAdapterRule> recipeRules = new ArrayList<>();

    private RecipeAdapterBuilder(ResourceLocation machineName, ResourceLocation parentMachineName) {
        super(null, machineName, 0, 0, false);
        this.parentMachineName = parentMachineName;
    }

    @ZenMethod
    public static RecipeAdapterBuilder create(String machineName, String parentMachineName) {
        ResourceLocation machineLoc = new ResourceLocation(machineName);
        if (machineLoc.getNamespace().equals("minecraft")) {
            machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getPath());
        }
        return new RecipeAdapterBuilder(machineLoc, new ResourceLocation(parentMachineName));
    }

    //----------------------------------------------------------------------------------------------
    // Adapter
    //----------------------------------------------------------------------------------------------

    /**
     * 为配方适配器添加一个 RecipeModifier，RecipeModifier 的数值会在复制父机械的配方的时候被应用。
     * 如果使用链式调用此方法，必须第一个调用！
     * Adds a RecipeModifier to the recipe adapter. The value of the RecipeModifier will be applied when copying the recipe of the parent machine.
     * To call this method, it must be the first one called!
     *
     * @param modifier RecipeModifier
     */
    @ZenMethod
    public RecipeAdapterBuilder addModifier(RecipeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    /**
     * 选择原有输入包含全部指定材料的配方，支持物品、矿辞和流体，忽略数量。
     * 返回的规则需要调用 build() 保存，最后再调用适配器的 build() 注册。
     */
    @ZenMethod
    public RecipeAdapterRule forInput(IIngredient... inputs) {
        return new RecipeAdapterRule(this).forInput(inputs);
    }

    /**
     * 选择原有输出包含全部指定材料的配方，支持物品、矿辞和流体，忽略数量。
     */
    @ZenMethod
    public RecipeAdapterRule forOutput(IIngredient... outputs) {
        return new RecipeAdapterRule(this).forOutput(outputs);
    }

    /**
     * 选择原有输入中物品需求种类数恰好为 count 的配方，例如只挑出仅有一种物品输入的配方。
     * 每种物品需求（物品、矿辞、物品组）各计为一种；流体、气体和可选催化剂不计入。
     * 返回的规则需要调用 build() 保存，最后再调用适配器的 build() 注册。
     */
    @ZenMethod
    public RecipeAdapterRule forInputItemTypes(int count) {
        return new RecipeAdapterRule(this).forInputItemTypes(count);
    }

    /**
     * 选择原有输入中物品需求种类数在 [min, max] 区间内（含边界）的配方。
     */
    @ZenMethod
    public RecipeAdapterRule forInputItemTypes(int min, int max) {
        return new RecipeAdapterRule(this).forInputItemTypes(min, max);
    }

    /**
     * 选择原有输出中物品需求种类数恰好为 count 的配方。
     * 每种物品需求（物品、矿辞、物品组）各计为一种；流体、气体和可选催化剂不计入。
     */
    @ZenMethod
    public RecipeAdapterRule forOutputItemTypes(int count) {
        return new RecipeAdapterRule(this).forOutputItemTypes(count);
    }

    /**
     * 选择原有输出中物品需求种类数在 [min, max] 区间内（含边界）的配方。
     */
    @ZenMethod
    public RecipeAdapterRule forOutputItemTypes(int min, int max) {
        return new RecipeAdapterRule(this).forOutputItemTypes(min, max);
    }

    void addRecipeRule(RecipeAdapterRule rule) {
        recipeRules.add(rule);
    }

    /**
     * 将适配器及命中的规则应用到配方上。
     *
     * @return true 表示保留该配方；false 表示配方命中了 remove() 规则，不应注册。
     */
    public boolean applyTo(MachineRecipe recipe) {
        List<RecipeAdapterRule> matchingRules = new ArrayList<>();
        for (RecipeAdapterRule rule : recipeRules) {
            if (rule.matches(recipe)) {
                if (rule.isRemoval()) {
                    // 移除优先：命中的配方不再应用全局追加和其他规则。
                    return false;
                }
                matchingRules.add(rule);
            }
        }

        // 所有规则先按原有材料匹配，避免全局追加或前一条规则改变后续筛选结果。
        recipe.mergeAdapter(this);
        for (RecipeAdapterRule rule : matchingRules) {
            recipe.mergePrimer(rule);
        }
        return true;
    }

    public List<RecipeModifier> getModifiers() {
        return modifiers;
    }

    public ResourceLocation getAdapterParentMachineName() {
        return parentMachineName;
    }

    @Override
    public void loadNeedAfterInitActions() {
        super.loadNeedAfterInitActions();
        for (RecipeAdapterRule rule : recipeRules) {
            rule.loadNeedAfterInitActions();
        }
    }

    @Override
    @ZenMethod
    public void build() {
        RecipeRegistry.getRegistry().registerRecipeAdapterEarly(this);
    }

    @Override
    @ZenMethod
    public RecipeAdapterBuilder setLoadJEI(boolean load) {
        super.setLoadJEI(load);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterBuilder setJEICategory(String key) {
        super.setJEICategory(key);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterBuilder setJEICategory(String key, String displayName) {
        super.setJEICategory(key, displayName);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterBuilder setJEICategory(String key, IItemStack icon) {
        super.setJEICategory(key, icon);
        return this;
    }

    @Override
    @ZenMethod
    public RecipeAdapterBuilder setJEICategory(String key, String displayName, IItemStack icon) {
        super.setJEICategory(key, displayName, icon);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Unavailable features
    //----------------------------------------------------------------------------------------------
    @Override
    public RecipePrimer setParallelized(boolean isParallelized) {
        CraftTweakerAPI.logWarning("[ModularMachinery] RecipeAdapterBuilder cannot set parallelized, it depends on the parent machine recipe!");
        return this;
    }
}
