package hellfirepvp.modularmachinery.common.crafting.helper;

import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import github.kasuminova.mmce.common.util.MultiFluidTank;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementDuration;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItem;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RecipeInputCheckTest {
    @BeforeClass
    public static void initializeTypes() {
        Bootstrap.register();
        RequirementTypesMM.REQUIREMENT_ITEM = new RequirementTypeItem();
        RequirementTypesMM.REQUIREMENT_ITEM.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_ITEM);
        RequirementTypesMM.REQUIREMENT_FLUID = new RequirementTypeFluid();
        RequirementTypesMM.REQUIREMENT_FLUID.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_FLUID);
        RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY = new RequirementTypeIngredientArray();
        RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_INGREDIENT_ARRAY);
        RequirementTypesMM.REQUIREMENT_DURATION = new RequirementDuration();
        RequirementTypesMM.REQUIREMENT_DURATION.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_DURATION);
        ComponentTypesMM.COMPONENT_ITEM = componentType(ComponentTypesMM.KEY_COMPONENT_ITEM);
        ComponentTypesMM.COMPONENT_FLUID = componentType(ComponentTypesMM.KEY_COMPONENT_FLUID);
    }

    @Test
    public void 空输入报告缺料且不复制库存() {
        CountingItems items = new CountingItems();
        RecipeCraftingContext context = context(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)), items, null);
        assertEquals("craftcheck.failure.item.input", context.canStartCrafting().getFirstErrorMessage(""));
        assertEquals(27, items.stackReads);
    }

    @Test
    public void 空物品输入不阻止纯流体配方() {
        FluidTank water = new FluidTank(new FluidStack(FluidRegistry.WATER, 1000), 1000);
        RecipeCraftingContext context = context(waterRequirement(), new ItemStackHandler(27), water);
        assertTrue(context.canStartCrafting().isSuccess());
        assertEquals(1000, water.getFluidAmount());
    }

    @Test
    public void 共用物品和流体参与隔离输入判断() {
        ItemStackHandler sharedItems = new ItemStackHandler(1);
        sharedItems.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        InfItemFluidHandler input = new InfItemFluidHandler(sharedItems,
            new FluidTank(new FluidStack(FluidRegistry.WATER, 1000), 1000));
        assertTrue(input.isEmpty()); // 此方法只检查自身，不能用于配方输入预筛选。
        assertTrue(context(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)), input, null)
            .canStartCrafting().isSuccess());
        assertTrue(context(waterRequirement(), null, input).canStartCrafting().isSuccess());
    }

    @Test
    public void 隔离分组不能拼接材料但会继续寻找后续分组() {
        MachineRecipe recipe = recipe();
        recipe.addRequirement(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)));
        recipe.addRequirement(waterRequirement());
        TestController controller = new TestController();
        ItemStackHandler iron = new ItemStackHandler(1);
        iron.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        controller.group(0, iron, new FluidTank(1000));
        controller.group(1, new ItemStackHandler(1), new FluidTank(new FluidStack(FluidRegistry.WATER, 1000), 1000));
        RecipeCraftingContext context = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), controller);
        assertTrue(context.canStartCrafting().isFailure());
        controller.group(2, iron, new FluidTank(new FluidStack(FluidRegistry.WATER, 1000), 1000));
        context.updateComponents(controller.getFoundComponents());
        assertTrue(context.canStartCrafting().isSuccess());
        assertEquals(2, context.getGroupId());
        assertEquals(1, iron.getStackInSlot(0).getCount());
    }

    @Test
    public void 物品组需求也能快速报告空输入() {
        RequirementIngredientArray array = new RequirementIngredientArray(
            Collections.singletonList(new ChancedIngredientStack(new ItemStack(Items.IRON_INGOT))));
        CountingItems items = new CountingItems();
        assertEquals("craftcheck.failure.item.input", context(array, items, null).canStartCrafting().getFirstErrorMessage(""));
        assertEquals(27, items.stackReads);
    }

    @Test
    public void 检查前修改器可将消耗降到零() {
        RecipeCraftingContext fluid = context(waterRequirement(), null, new FluidTank(1000));
        fluid.addModifier(new RecipeModifier(RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, 0, 1, false));
        assertTrue(fluid.canStartCrafting().isSuccess());
        RecipeCraftingContext item = context(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)),
            new ItemStackHandler(1), null);
        item.addModifier(new RecipeModifier(RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, 0, 1, false));
        assertTrue(item.canStartCrafting().isSuccess());
        RequirementIngredientArray array = new RequirementIngredientArray(
            Collections.singletonList(new ChancedIngredientStack(new ItemStack(Items.IRON_INGOT))));
        RecipeCraftingContext ingredients = context(array, new ItemStackHandler(1), null);
        ingredients.addModifier(new RecipeModifier(RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, 0, 1, false));
        assertTrue(ingredients.canStartCrafting().isSuccess());
    }

    @Test
    public void 随机数量含零时不能快速拒绝() {
        RequirementItem item = new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT));
        item.minAmount = 0;
        item.maxAmount = 1;
        RecipeCraftingContext context = context(item, new ItemStackHandler(1), null);
        RecipeInputCheck check = RecipeInputCheck.create(Collections.singletonList(item), context);
        assertNotNull(check);
        assertNull(check.getMissingInput(Collections.singletonList(
            new RequirementComponents(item, context.getCurrentComponents().get(0).components()))));
    }

    @Test
    public void 自定义需求保持原检查流程() {
        RequirementItem custom = new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)) { };
        assertNull(RecipeInputCheck.create(Collections.singletonList(custom), null));
    }

    @Test
    public void 检查前回调可以补充输入() {
        FluidTank water = new FluidTank(1000);
        TestController controller = new TestController() {
            @Override
            public RecipeCraftingContext.CraftingCheckResult checkPreStartResult(RecipeCraftingContext context) {
                water.fill(new FluidStack(FluidRegistry.WATER, 100), true);
                return null;
            }
            @Override
            public RecipeCraftingContext.CraftingCheckResult checkStartResult(RecipeCraftingContext context,
                RecipeCraftingContext.CraftingCheckResult result) {
                return result;
            }
        };
        controller.group(0, null, water);
        MachineRecipe recipe = recipe();
        recipe.addRequirement(waterRequirement());
        RecipeCraftingContext context = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), controller);
        assertTrue(controller.onCheck(context).isSuccess());
    }

    @Test
    public void 空隔离分组不检查输出() {
        MachineRecipe recipe = recipe();
        recipe.addRequirement(waterRequirement());
        recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, new FluidStack(FluidRegistry.WATER, 100)));
        TestController controller = new TestController();
        CountingFluids output = new CountingFluids();
        for (int group = 0; group < 108; group++) {
            controller.group(group, null, new InfItemFluidHandler());
            controller.output(group, output);
        }
        RecipeCraftingContext context = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 4), controller);
        assertEquals("craftcheck.failure.fluid.input", context.canStartCrafting().getFirstErrorMessage(""));
        assertEquals(0, output.propertyReads);
    }

    @Test
    public void 流体种类不符或数量不足直接报告缺料() {
        assertTrue(context(waterRequirement(), null,
            new FluidTank(new FluidStack(FluidRegistry.LAVA, 1000), 1000)).canStartCrafting().isFailure());
        CountingFluids water = new CountingFluids();
        water.fill(new FluidStack(FluidRegistry.WATER, 99), true);
        assertEquals("craftcheck.failure.fluid.input", context(waterRequirement(), null, water)
            .canStartCrafting().getFirstErrorMessage(""));
        assertEquals(1, water.propertyReads);
    }

    @Test
    public void 流体可以合并自身与共用槽位但不能跨隔离分组() {
        InfItemFluidHandler input = new InfItemFluidHandler(new FluidTank(new FluidStack(FluidRegistry.WATER, 60), 1000));
        input.fill(new FluidStack(FluidRegistry.WATER, 40), true);
        assertTrue(context(waterRequirement(), null, input).canStartCrafting().isSuccess());
        assertEquals(40, input.getFluidStackList().get(0).amount);
        MachineRecipe recipe = recipe();
        recipe.addRequirement(waterRequirement());
        TestController controller = new TestController();
        controller.group(0, null, new FluidTank(new FluidStack(FluidRegistry.WATER, 60), 1000));
        controller.group(1, null, new FluidTank(new FluidStack(FluidRegistry.WATER, 40), 1000));
        assertTrue(new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), controller).canStartCrafting().isFailure());
    }

    @Test
    public void 流体匹配保留NBT条件() {
        NBTTagCompound requiredTag = new NBTTagCompound();
        requiredTag.setString("等级", "高");
        FluidStack required = new FluidStack(FluidRegistry.WATER, 100, requiredTag);
        NBTTagCompound otherTag = new NBTTagCompound();
        otherTag.setString("等级", "低");
        assertTrue(context(new RequirementFluid(IOType.INPUT, required), null,
            new FluidTank(new FluidStack(FluidRegistry.WATER, 100, otherTag), 1000)).canStartCrafting().isFailure());
        assertTrue(context(new RequirementFluid(IOType.INPUT, required), null,
            new FluidTank(new FluidStack(FluidRegistry.WATER, 100, requiredTag), 1000)).canStartCrafting().isSuccess());
    }

    @Test
    public void 流体预检查不能代替累计消耗的精确校验() {
        MachineRecipe recipe = recipe();
        recipe.addRequirement(waterRequirement());
        recipe.addRequirement(waterRequirement());
        TestController controller = new TestController();
        FluidTank water = new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 1000);
        controller.group(0, null, water);
        RecipeCraftingContext context = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), controller);
        assertTrue(context.canStartCrafting().isFailure());
        assertEquals(100, water.getFluidAmount());
        water.fill(new FluidStack(FluidRegistry.WATER, 100), true);
        assertTrue(context.canStartCrafting().isSuccess());
        assertEquals(200, water.getFluidAmount());
    }

    // 基准复用上下文；空分组和多槽流体输入分别对应反应、离心单元的实机热点。
    public static void main(String[] args) {
        initializeTypes();
        boolean waterOnly = args.length > 0 && args[0].equals("water-only");
        int groups = waterOnly ? 1 : 108;
        TestController controller = new TestController();
        CountingFluids output = new CountingFluids();
        for (int group = 0; group < groups; group++) {
            MultiFluidTank water = new MultiFluidTank(1000, 19);
            water.fill(new FluidStack(FluidRegistry.WATER, 4000), true);
            controller.group(group, null, waterOnly ? water : new InfItemFluidHandler());
            controller.output(group, output);
            if (waterOnly) controller.outputItems(group, new ItemStackHandler(27));
        }
        MachineRecipe[] recipes = new MachineRecipe[waterOnly ? 532 : 333];
        RecipeCraftingContext[] contexts = new RecipeCraftingContext[recipes.length];
        for (int i = 0; i < recipes.length; i++) {
            recipes[i] = recipe();
            recipes[i].addRequirement(waterOnly ? new RequirementFluid(IOType.INPUT, new FluidStack(FluidRegistry.LAVA, 100))
                : waterRequirement());
            recipes[i].addRequirement(new RequirementFluid(IOType.OUTPUT, new FluidStack(FluidRegistry.WATER, 100)));
            if (waterOnly) {
                recipes[i].addRequirement(new RequirementFluid(IOType.OUTPUT, new FluidStack(FluidRegistry.WATER, 100)));
                recipes[i].addRequirement(new RequirementFluid(IOType.OUTPUT, new FluidStack(FluidRegistry.WATER, 100)));
                recipes[i].addRequirement(new RequirementItem(IOType.OUTPUT, new ItemStack(Items.IRON_INGOT)));
            }
            contexts[i] = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipes[i], 4), controller);
            contexts[i].resetAll();
        }
        long[] times = new long[30];
        int searches = 0;
        long warmupEnd = System.nanoTime() + 3_000_000_000L;
        do {
            checkRecipes(contexts, recipes, controller);
            searches++;
        } while (System.nanoTime() < warmupEnd);
        for (int run = 0; run < times.length; run++) {
            long start = System.nanoTime();
            checkRecipes(contexts, recipes, controller);
            times[run] = System.nanoTime() - start;
            searches++;
        }
        java.util.Arrays.sort(times);
        System.out.println(recipes.length + " × " + groups + (waterOnly ? " 流体种类不符" : " 空输入")
            + "，复用上下文并刷新组件；中位数 " + times[times.length / 2] / 1_000_000.0 + " ms");
        System.out.println("单次搜索输出属性读取次数 " + output.propertyReads / searches);
    }

    private static void checkRecipes(RecipeCraftingContext[] contexts, MachineRecipe[] recipes, TestController controller) {
        for (int i = 0; i < recipes.length; i++) {
            RecipeCraftingContext context = contexts[i].init(new ActiveMachineRecipe(recipes[i], 4), controller);
            if (!context.canStartCrafting().isFailure()) throw new AssertionError("输入不足应失败");
            context.resetAll();
        }
    }

    private static ComponentType componentType(ResourceLocation name) {
        ComponentType type = new ComponentType() {
            @Override
            public String requiresModid() { return null; }
        };
        type.setRegistryName(name);
        return type;
    }

    private static RequirementFluid waterRequirement() {
        return new RequirementFluid(IOType.INPUT, new FluidStack(FluidRegistry.WATER, 100));
    }

    private static MachineRecipe recipe() {
        return new MachineRecipe("测试", new ResourceLocation("test", "recipe"), new ResourceLocation("test", "machine"), 20, 0, false, true);
    }

    private static RecipeCraftingContext context(ComponentRequirement<?, ?> requirement, IItemHandlerModifiable items, IFluidHandler fluids) {
        MachineRecipe recipe = recipe();
        recipe.addRequirement(requirement);
        TestController controller = new TestController();
        controller.group(0, items, fluids);
        return new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), controller);
    }

    private static class CountingItems extends ItemStackHandler {
        int stackReads;
        CountingItems() { super(27); }
        @Override
        public ItemStack getStackInSlot(int slot) {
            stackReads++;
            return super.getStackInSlot(slot);
        }
    }

    private static class CountingFluids extends FluidTank {
        int propertyReads;
        CountingFluids() { super(1000); }
        @Override
        public net.minecraftforge.fluids.capability.IFluidTankProperties[] getTankProperties() {
            propertyReads++;
            return super.getTankProperties();
        }
    }

    private static class TestController extends TileMachineController {
        void outputItems(long id, IItemHandlerModifiable items) {
            MachineComponent.ItemBus bus = new MachineComponent.ItemBus(IOType.OUTPUT) {
                @Override
                public IItemHandlerModifiable getContainerProvider() { return items; }
            };
            foundComponents.get(id).put(new TileEntity() { }, new ProcessingComponent<>(bus, items, null));
        }
        void output(long id, IFluidHandler fluids) {
            MachineComponent.FluidHatch hatch = new MachineComponent.FluidHatch(IOType.OUTPUT) {
                @Override
                public IFluidHandler getContainerProvider() { return fluids; }
            };
            foundComponents.get(id).put(new TileEntity() { }, new ProcessingComponent<>(hatch, fluids, null));
        }
        void group(long id, IItemHandlerModifiable items, IFluidHandler fluids) {
            Map<TileEntity, ProcessingComponent<?>> components = new HashMap<>();
            if (items != null) {
                MachineComponent.ItemBus bus = new MachineComponent.ItemBus(IOType.INPUT) {
                    @Override
                    public IItemHandlerModifiable getContainerProvider() { return items; }
                };
                components.put(new TileEntity() { }, new ProcessingComponent<>(bus, items, null));
            }
            if (fluids != null) {
                MachineComponent.FluidHatch hatch = new MachineComponent.FluidHatch(IOType.INPUT) {
                    @Override
                    public IFluidHandler getContainerProvider() { return fluids; }
                };
                components.put(new TileEntity() { }, new ProcessingComponent<>(hatch, fluids, null));
            }
            foundComponents.put(id, components);
        }
        @Override
        public void doControllerTick() { }
        @Override
        public CraftingStatus getControllerStatus() { return CraftingStatus.IDLE; }
        @Override
        public void setControllerStatus(CraftingStatus status) { }
        @Override
        public void flushContextModifier() { }
    }
}
