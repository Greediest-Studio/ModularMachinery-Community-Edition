/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.common.container.handler.MEInputRecipeTransferHandler;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidRenderer;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridStackHelper;
import hellfirepvp.modularmachinery.common.integration.preview.CategoryStructurePreview;
import hellfirepvp.modularmachinery.common.integration.preview.StructurePreviewWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.CategoryDynamicRecipe;
import hellfirepvp.modularmachinery.common.integration.recipe.DynamicRecipeWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutHelper;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.MachineJEICategory;
import mezz.jei.Internal;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.Config;
import mezz.jei.input.InputHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModIntegrationJEI
 * Created by HellFirePvP
 * Date: 10.07.2017 / 19:22
 */
@JEIPlugin
public class ModIntegrationJEI implements IModPlugin {
    public static final  String                                                           CATEGORY_PREVIEW             = "modularmachinery.preview";
    public static final  List<StructurePreviewWrapper>                                    PREVIEW_WRAPPERS             = Lists.newArrayList();
    private static final Map<String, CategoryDynamicRecipe>                               RECIPE_CATEGORIES_BY_UID     = new LinkedHashMap<>();
    private static final Map<DynamicMachine, Map<String, CategoryDynamicRecipe>>          RECIPE_CATEGORIES_BY_MACHINE = new LinkedHashMap<>();
    private static final Map<String, Map<ResourceLocation, DynamicRecipeWrapper>>         CATEGORY_RECIPE_WRAPPERS    = new LinkedHashMap<>();

    public static Field inputHandler = null;
    public static Field bookmarkList = null;

    public static IStackHelper        stackHelper;
    public static IJeiHelpers         jeiHelpers;
    public static IIngredientRegistry ingredientRegistry;
    public static IRecipeRegistry     recipeRegistry;
    public static IJeiRuntime         jeiRuntime;

    static {
        // I Just want to get the BookmarkList...
        try {
            Field inputHandler = Internal.class.getDeclaredField("inputHandler");
            inputHandler.setAccessible(true);
            ModIntegrationJEI.inputHandler = inputHandler;

            Field bookmarkList = InputHandler.class.getDeclaredField("bookmarkList");
            bookmarkList.setAccessible(true);
            ModIntegrationJEI.bookmarkList = bookmarkList;
        } catch (NoSuchFieldException e) {
            ModularMachinery.log.warn(e);
        }
    }

    public static String getCategoryStringFor(DynamicMachine machine) {
        return "modularmachinery.recipes." + machine.getRegistryName().getPath();
    }

    public static String getCategoryStringFor(DynamicMachine machine, @Nullable String subCategoryKey) {
        if (subCategoryKey == null || subCategoryKey.isEmpty()) {
            return getCategoryStringFor(machine);
        }
        return "modularmachinery.recipes." + machine.getRegistryName().getPath() + "." + subCategoryKey;
    }

    public static CategoryDynamicRecipe getCategory(DynamicMachine machine) {
        return getCategory(machine, null);
    }

    public static CategoryDynamicRecipe getCategory(DynamicMachine machine, @Nullable String subCategoryKey) {
        Map<String, CategoryDynamicRecipe> map = RECIPE_CATEGORIES_BY_MACHINE.get(machine);
        if (map == null) {
            return null;
        }
        CategoryDynamicRecipe cat = map.get(subCategoryKey != null ? subCategoryKey : "");
        if (cat == null && subCategoryKey != null) {
            cat = map.get("");
        }
        return cat;
    }

    public static CategoryDynamicRecipe getCategoryByUid(String uid) {
        return RECIPE_CATEGORIES_BY_UID.get(uid);
    }

    public static void reloadRecipeWrappers() {
        RECIPE_CATEGORIES_BY_UID.values().forEach(CategoryDynamicRecipe::reloadCategory);

        for (Map.Entry<String, Map<ResourceLocation, DynamicRecipeWrapper>> catEntry : CATEGORY_RECIPE_WRAPPERS.entrySet()) {
            String categoryUid = catEntry.getKey();
            CategoryDynamicRecipe category = RECIPE_CATEGORIES_BY_UID.get(categoryUid);
            if (category == null) {
                continue;
            }
            Map<ResourceLocation, DynamicRecipeWrapper> wrappers = catEntry.getValue();
            for (MachineRecipe recipe : category.getCategoryRecipes()) {
                if (!recipe.getLoadJEI()) {
                    continue;
                }
                DynamicRecipeWrapper wrapper = wrappers.get(recipe.getRegistryName());
                if (wrapper != null) {
                    wrapper.reloadWrapper(recipe);
                }
            }
        }
    }

    public static void reloadPreviewWrappers() {
//        PREVIEW_WRAPPERS.forEach(StructurePreviewWrapper::flushContext);
    }

    public static void addItemStackToBookmarkList(ItemStack stack) {
        if (inputHandler == null || bookmarkList == null || stack.isEmpty()) {
            return;
        }

        try {
            InputHandler handler = (InputHandler) inputHandler.get(null);
            BookmarkList bookmark = (BookmarkList) bookmarkList.get(handler);

            if (!Config.isBookmarkOverlayEnabled()) {
                Config.toggleBookmarkEnabled();
            }
            bookmark.add(stack);
        } catch (IllegalAccessException e) {
            ModularMachinery.log.warn(e);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(ItemsMM.blueprint, (s) -> {
            DynamicMachine machine = ItemBlueprint.getAssociatedMachine(s);
            if (machine == null) {
                return ISubtypeRegistry.ISubtypeInterpreter.NONE;
            }
            return machine.getRegistryName().toString();
        });
    }

    /**
     * AE2FCR need this.
     */
    @Override
    @Deprecated
    public void registerIngredients(@NotNull IModIngredientRegistration registry) {
        try {
            registry.register(() -> HybridFluid.class, Lists.newArrayList(), new HybridStackHelper<>(), new HybridFluidRenderer<>());
            if (Mods.MEKANISM.isPresent()) {
                registerHybridGas(registry);
            }
        } catch (Exception exc) {
            ModularMachinery.log.warn("Error setting up HybridFluid JEI registration! Check the log after this for more details! Report this error!");
            exc.printStackTrace();
            throw exc;
        }
    }


    @Optional.Method(modid = "mekanism")
    private void registerHybridGas(IModIngredientRegistration registry) {
        registry.register(() -> HybridFluidGas.class, Lists.newArrayList(), new HybridStackHelper<>(), new HybridFluidRenderer<>());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        RecipeLayoutHelper.init();

        RecipeRegistry.reloadAdapters();

        registry.addRecipeCategories(new CategoryStructurePreview());

        RECIPE_CATEGORIES_BY_UID.clear();
        RECIPE_CATEGORIES_BY_MACHINE.clear();

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            Iterable<MachineRecipe> allRecipes = RecipeRegistry.getRecipesFor(machine);

            // Group recipes by categoryKey
            Map<String, List<MachineRecipe>> recipesByCategory = new LinkedHashMap<>();
            recipesByCategory.put("", new ArrayList<>());

            for (MachineRecipe recipe : allRecipes) {
                if (!recipe.getLoadJEI()) {
                    continue;
                }
                String key = recipe.getJEICategoryKey();
                String normKey = (key == null || key.isEmpty()) ? "" : key;
                recipesByCategory.computeIfAbsent(normKey, k -> new ArrayList<>()).add(recipe);
            }

            // Also check categories defined in DynamicMachine
            for (String key : machine.getJEICategories().keySet()) {
                recipesByCategory.putIfAbsent(key, new ArrayList<>());
            }

            List<MachineRecipe> defaultRecipes = recipesByCategory.get("");
            boolean hasSubCategories = recipesByCategory.keySet().stream().anyMatch(k -> !k.isEmpty() && !recipesByCategory.get(k).isEmpty());

            // 1. Default category:
            // Register default category if it has recipes, OR if there are no subcategories at all.
            if (!defaultRecipes.isEmpty() || !hasSubCategories) {
                String defaultUid = getCategoryStringFor(machine);
                CategoryDynamicRecipe defaultCategory = new CategoryDynamicRecipe(
                    machine,
                    null,
                    defaultUid,
                    machine.getLocalizedName(),
                    ItemStack.EMPTY,
                    () -> {
                        List<MachineRecipe> list = new ArrayList<>();
                        for (MachineRecipe r : RecipeRegistry.getRecipesFor(machine)) {
                            if (r.getLoadJEI() && (r.getJEICategoryKey() == null || r.getJEICategoryKey().isEmpty())) {
                                list.add(r);
                            }
                        }
                        return list;
                    }
                );
                RECIPE_CATEGORIES_BY_UID.put(defaultUid, defaultCategory);
                RECIPE_CATEGORIES_BY_MACHINE.computeIfAbsent(machine, k -> new LinkedHashMap<>()).put("", defaultCategory);
                registry.addRecipeCategories(defaultCategory);
            }

            // 2. Subcategories:
            List<String> subKeys = new ArrayList<>(recipesByCategory.keySet());
            subKeys.remove("");
            subKeys.sort((k1, k2) -> {
                MachineJEICategory c1 = machine.getJEICategory(k1);
                MachineJEICategory c2 = machine.getJEICategory(k2);
                int p1 = c1 != null ? c1.getPriority() : 0;
                int p2 = c2 != null ? c2.getPriority() : 0;
                return Integer.compare(p1, p2);
            });

            for (String subKey : subKeys) {
                List<MachineRecipe> subRecipes = recipesByCategory.get(subKey);
                MachineJEICategory definedCat = machine.getJEICategory(subKey);
                if ((subRecipes == null || subRecipes.isEmpty()) && definedCat == null) {
                    continue;
                }

                String subUid = getCategoryStringFor(machine, subKey);
                String displayName = null;
                ItemStack iconStack = ItemStack.EMPTY;

                if (definedCat != null) {
                    displayName = definedCat.getDisplayName();
                    iconStack = definedCat.getIcon();
                }

                if ((displayName == null || displayName.isEmpty()) && subRecipes != null) {
                    for (MachineRecipe r : subRecipes) {
                        if (r.getJEICategoryName() != null && !r.getJEICategoryName().isEmpty()) {
                            displayName = r.getJEICategoryName();
                            break;
                        }
                    }
                }
                if (iconStack.isEmpty() && subRecipes != null) {
                    for (MachineRecipe r : subRecipes) {
                        if (r.getJEICategoryIcon() != null && !r.getJEICategoryIcon().isEmpty()) {
                            iconStack = r.getJEICategoryIcon();
                            break;
                        }
                    }
                }

                String title;
                String langKey = "modularmachinery.jei.category." + machine.getRegistryName().getPath() + "." + subKey;
                if (displayName != null && !displayName.isEmpty()) {
                    title = I18n.hasKey(displayName) ? I18n.format(displayName) : displayName;
                } else if (I18n.hasKey(langKey)) {
                    title = I18n.format(langKey);
                } else {
                    title = machine.getLocalizedName() + " - " + subKey;
                }

                final String finalKey = subKey;
                CategoryDynamicRecipe category = new CategoryDynamicRecipe(
                    machine,
                    subKey,
                    subUid,
                    title,
                    iconStack,
                    () -> {
                        List<MachineRecipe> list = new ArrayList<>();
                        for (MachineRecipe r : RecipeRegistry.getRecipesFor(machine)) {
                            if (r.getLoadJEI() && finalKey.equals(r.getJEICategoryKey())) {
                                list.add(r);
                            }
                        }
                        return list;
                    }
                );

                RECIPE_CATEGORIES_BY_UID.put(subUid, category);
                RECIPE_CATEGORIES_BY_MACHINE.computeIfAbsent(machine, k -> new LinkedHashMap<>()).put(subKey, category);
                registry.addRecipeCategories(category);
            }
        }
    }

    @Override
    public void register(IModRegistry registry) {
        jeiHelpers = registry.getJeiHelpers();
        ingredientRegistry = registry.getIngredientRegistry();
        RecipeLayoutHelper.init();

        registry.addRecipeCatalyst(new ItemStack(BlocksMM.blockController), CATEGORY_PREVIEW);
        registry.addRecipeCatalyst(new ItemStack(BlocksMM.blockFactoryController), CATEGORY_PREVIEW);

        CATEGORY_RECIPE_WRAPPERS.clear();

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            PREVIEW_WRAPPERS.add(new StructurePreviewWrapper(machine));
        }

        registry.addRecipes(PREVIEW_WRAPPERS, CATEGORY_PREVIEW);

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            Map<String, CategoryDynamicRecipe> catMap = RECIPE_CATEGORIES_BY_MACHINE.get(machine);
            if (catMap == null) {
                continue;
            }

            for (CategoryDynamicRecipe category : catMap.values()) {
                String categoryUid = category.getUid();

                // 1. Catalyst: Custom Icon (if specified, added first)
                if (category.getIconStack() != null && !category.getIconStack().isEmpty()) {
                    registry.addRecipeCatalyst(category.getIconStack(), categoryUid);
                }

                // 2. Catalyst: Blueprint
                ItemStack blueprintStack = new ItemStack(ItemsMM.blueprint);
                ItemBlueprint.setAssociatedMachine(blueprintStack, machine);
                registry.addRecipeCatalyst(blueprintStack, categoryUid);

                // 3. Catalysts: Machine Controllers
                BlockController controller = BlockController.MACHINE_CONTROLLERS.get(machine);
                if (controller != null) {
                    registry.addRecipeCatalyst(new ItemStack(controller), categoryUid);
                }
                BlockController mocController = BlockController.MOC_MACHINE_CONTROLLERS.get(machine);
                if (mocController != null) {
                    registry.addRecipeCatalyst(new ItemStack(mocController), categoryUid);
                }
                BlockFactoryController fController = BlockFactoryController.FACTORY_CONTROLLERS.get(machine);
                if (fController != null) {
                    registry.addRecipeCatalyst(new ItemStack(fController), categoryUid);
                }

                // 4. AE2 Transfer handler
                if (Mods.AE2.isPresent()) {
                    registry.getRecipeTransferRegistry().addRecipeTransferHandler(new MEInputRecipeTransferHandler(), categoryUid);
                }

                // 5. Recipes
                Map<ResourceLocation, DynamicRecipeWrapper> wrappers = CATEGORY_RECIPE_WRAPPERS.computeIfAbsent(categoryUid, v -> new LinkedHashMap<>());
                for (MachineRecipe recipe : category.getCategoryRecipes()) {
                    if (!recipe.getLoadJEI()) {
                        continue;
                    }
                    wrappers.put(recipe.getRegistryName(), new DynamicRecipeWrapper(category, recipe));
                }
                registry.addRecipes(wrappers.values(), categoryUid);
            }
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeRegistry = jeiRuntime.getRecipeRegistry();
        ModIntegrationJEI.jeiRuntime = jeiRuntime;
    }

}
