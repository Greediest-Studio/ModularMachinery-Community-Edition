package github.kasuminova.mmce.common;

import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import github.kasuminova.mmce.common.upgrade.SimpleMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.UpgradeType;
import github.kasuminova.mmce.common.util.concurrent.ExecuteGroup;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentFluid;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentItem;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentUpgradeBus;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementDuration;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItem;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OptimizationRegressionTest {

    @BeforeClass
    public static void initTypes() {
        Bootstrap.register();
        if (ComponentTypesMM.COMPONENT_ITEM == null) {
            ComponentTypesMM.COMPONENT_ITEM = new ComponentItem();
            ComponentTypesMM.COMPONENT_ITEM.setRegistryName(ComponentTypesMM.KEY_COMPONENT_ITEM);
        }
        if (ComponentTypesMM.COMPONENT_FLUID == null) {
            ComponentTypesMM.COMPONENT_FLUID = new ComponentFluid();
            ComponentTypesMM.COMPONENT_FLUID.setRegistryName(ComponentTypesMM.KEY_COMPONENT_FLUID);
        }
        if (ComponentTypesMM.COMPONENT_UPGRADE_BUS == null) {
            ComponentTypesMM.COMPONENT_UPGRADE_BUS = new ComponentUpgradeBus();
            ComponentTypesMM.COMPONENT_UPGRADE_BUS.setRegistryName(ComponentTypesMM.KEY_COMPONENT_UPGRADE_BUS);
        }
        if (RequirementTypesMM.REQUIREMENT_ITEM == null) {
            RequirementTypesMM.REQUIREMENT_ITEM = new RequirementTypeItem();
            RequirementTypesMM.REQUIREMENT_ITEM.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_ITEM);
        }
        if (RequirementTypesMM.REQUIREMENT_FLUID == null) {
            RequirementTypesMM.REQUIREMENT_FLUID = new RequirementTypeFluid();
            RequirementTypesMM.REQUIREMENT_FLUID.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_FLUID);
        }
        if (RequirementTypesMM.REQUIREMENT_DURATION == null) {
            RequirementTypesMM.REQUIREMENT_DURATION = new RequirementDuration();
            RequirementTypesMM.REQUIREMENT_DURATION.setRegistryName(RequirementTypesMM.KEY_REQUIREMENT_DURATION);
        }
    }

    @Test
    public void executeGroupClaimAndPendingState() {
        ExecuteGroup group = new ExecuteGroup(ExecuteGroup.newGroupId());
        assertTrue(group.isEmpty());
        assertFalse(group.isSubmitted());

        assertTrue(group.markPending());
        assertFalse(group.markPending());
        group.clearPending();
        assertTrue(group.markPending());

        assertTrue(group.tryClaimSubmit());
        assertFalse(group.tryClaimSubmit());
        group.setSubmitted(false);
        assertTrue(group.tryClaimSubmit());
    }

    @Test
    public void taskExecutorRunsGroupedAndNestedSameGroupTasksInOrder() throws Exception {
        TaskExecutor executor = new TaskExecutor();
        executor.init();
        try {
            List<String> log = new CopyOnWriteArrayList<>();
            long groupA = ExecuteGroup.newGroupId();
            long groupB = ExecuteGroup.newGroupId();

            executor.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
            executor.addExecuteGroupTask(() -> {
                log.add("A1");
                executor.addExecuteGroupTask(() -> log.add("A2"), groupA);
            }, groupA);
            executor.addExecuteGroupTask(() -> log.add("B1"), groupB);
            executor.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));

            assertEquals(3, log.size());
            assertTrue(log.indexOf("A1") < log.indexOf("A2"));
            assertTrue(log.contains("B1"));
        } finally {
            Field submitterField = TaskExecutor.class.getDeclaredField("submitter");
            submitterField.setAccessible(true);
            TaskExecutor.TaskSubmitter submitter = (TaskExecutor.TaskSubmitter) submitterField.get(executor);
            if (submitter.thread != null) {
                submitter.thread.interrupt();
                submitter.unpark();
                submitter.thread.join(2000L);
            }
        }
    }

    @Test
    public void upgradeStaticModifiersCopyAndStackAndCustomDataDirtyTracking() {
        UpgradeType type = new UpgradeType("test_speed_upgrade", "Speed", 1.0F, 4);
        SimpleMachineUpgrade template = new SimpleMachineUpgrade(type);
        RecipeModifier speedMod = new RecipeModifier(RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 0.5F, RecipeModifier.OPERATION_MULTIPLY, false);
        template.addStaticModifier(true, "speed_key", speedMod);

        SimpleMachineUpgrade copied = template.copy(ItemStack.EMPTY);
        copied.incrementStackSize(1); // stackSize = 2
        Map<String, RecipeModifier> collected = new HashMap<>();
        copied.collectStaticModifiers(collected);
        assertTrue(collected.containsKey("speed_key"));
        assertEquals(0.125F, collected.get("speed_key").getModifier(), 1.0e-6F);

        SimpleMachineUpgrade nonStackable = new SimpleMachineUpgrade(new UpgradeType("test_fixed", "Fixed", 1.0F, 4));
        nonStackable.addStaticModifier(false, "fixed_key", speedMod);
        nonStackable.incrementStackSize(3);
        Map<String, RecipeModifier> fixedCollected = new HashMap<>();
        nonStackable.collectStaticModifiers(fixedCollected);
        assertEquals(0.5F, fixedCollected.get("fixed_key").getModifier(), 1.0e-6F);

        // Custom data dirty tracking: unchanged read does not trigger writeback or markNoUpdateSync.
        AtomicInteger syncCount = new AtomicInteger();
        TileUpgradeBus bus = new TileUpgradeBus() {
            @Override
            public void markNoUpdateSync() {
                syncCount.incrementAndGet();
            }
        };
        TileUpgradeBus.UpgradeBusProvider provider = bus.provideComponent();

        TestableSimpleUpgrade tracked = new TestableSimpleUpgrade(type);
        NBTTagCompound tag = new NBTTagCompound();
        tracked.readNBT(tag);
        assertFalse(tracked.hasCustomDataModified());

        // Reading without mutating leaves customData clean.
        tracked.accessTag(tag);
        assertFalse(tracked.isDirty(tag));

        // Mutating after read marks dirty.
        tag.setInteger("ticks", 5);
        assertTrue(tracked.isDirty(tag));

        // Replacing with equal copy stays clean and empty tag writes skip markNoUpdateSync.
        NBTTagCompound baseline = new NBTTagCompound();
        tracked.readNBT(baseline);
        tracked.replaceTag(baseline);
        assertFalse(tracked.isDirty(new NBTTagCompound()));
        provider.setUpgradeCustomData(tracked, new NBTTagCompound());
        assertEquals(0, syncCount.get());

        // Setting a non-empty compound marks dirty and triggers persistence once.
        NBTTagCompound changed = new NBTTagCompound();
        changed.setInteger("charge", 42);
        tracked.readNBT(new NBTTagCompound());
        tracked.replaceTag(new NBTTagCompound());
        assertTrue(tracked.isDirty(changed));
        provider.setUpgradeCustomData(tracked, changed);
        assertEquals(1, syncCount.get());

        // Clearing an existing non-empty tag removes it and syncs once.
        provider.setUpgradeCustomData(tracked, new NBTTagCompound());
        assertEquals(2, syncCount.get());
        provider.setUpgradeCustomData(tracked, new NBTTagCompound());
        assertEquals(2, syncCount.get());
    }

    @Test
    public void controllerHasModifierIncludesUpgradesAndFactoryFlushesCoreThreads() throws Exception {
        DynamicMachine machine = new DynamicMachine("test_factory");
        machine.addCoreThread(FactoryRecipeThread.createCoreThread("core"));

        TileFactoryController factory = new TileFactoryController() {
            @Override
            public void markNoUpdateSync() {
            }
        };
        Field foundMachineField = TileMultiblockMachineController.class.getDeclaredField("foundMachine");
        foundMachineField.setAccessible(true);
        foundMachineField.set(factory, machine);

        java.lang.reflect.Method updateCoreThread = TileFactoryController.class.getDeclaredMethod("updateCoreThread");
        updateCoreThread.setAccessible(true);
        updateCoreThread.invoke(factory);

        Field coreThreadsField = TileFactoryController.class.getDeclaredField("coreRecipeThreads");
        coreThreadsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, FactoryRecipeThread> coreThreads = (Map<String, FactoryRecipeThread>) coreThreadsField.get(factory);
        FactoryRecipeThread core = coreThreads.get("core");
        assertNotNull(core);

        MachineRecipe recipe = new MachineRecipe("test", new ResourceLocation("test", "r1"), machine.getRegistryName(), 100, 0, false, false);
        RecipeCraftingContext ctx = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe, 1), factory);
        core.setContext(ctx);

        UpgradeType type = new UpgradeType("core_speed", "CoreSpeed", 1.0F, 1);
        SimpleMachineUpgrade upgrade = new SimpleMachineUpgrade(type);
        RecipeModifier speedMod = new RecipeModifier(RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 0.5F, RecipeModifier.OPERATION_MULTIPLY, false);
        upgrade.addStaticModifier(false, "upgrade_speed", speedMod);

        Field foundUpgradesField = TileMultiblockMachineController.class.getDeclaredField("foundUpgrades");
        foundUpgradesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, List<MachineUpgrade>> foundUpgrades = (Map<String, List<MachineUpgrade>>) foundUpgradesField.get(factory);
        foundUpgrades.put(type.getName(), new ArrayList<>(Collections.singletonList(upgrade)));

        java.lang.reflect.Method refreshUpgradeModifiers = TileMultiblockMachineController.class.getDeclaredMethod("refreshUpgradeModifiers");
        refreshUpgradeModifiers.setAccessible(true);
        refreshUpgradeModifiers.invoke(factory);

        assertTrue(factory.hasModifier("upgrade_speed"));
        assertFalse(factory.hasModifier("missing_key"));

        factory.flushContextModifier();
        assertEquals(50.0D, RecipeModifier.applyModifiers(ctx, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 100.0D, false), 1.0e-6D);
    }

    @Test
    public void batchedContextModifiersAndSearchCacheIsolation() {
        TestController ctrlA = new TestController();
        ItemStackHandler iron = new ItemStackHandler(1);
        iron.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        ctrlA.group(0, iron, new FluidTank(new FluidStack(FluidRegistry.WATER, 1000), 1000));

        TestController ctrlB = new TestController();
        ctrlB.group(0, new ItemStackHandler(1), null);

        MachineRecipe recipe1 = new MachineRecipe("r1", new ResourceLocation("test", "r1"), new ResourceLocation("test", "m"), 100, 0, false, false);
        recipe1.addRequirement(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)));
        MachineRecipe recipe2 = new MachineRecipe("r2", new ResourceLocation("test", "r2"), new ResourceLocation("test", "m"), 100, 0, false, false);
        recipe2.addRequirement(new RequirementItem(IOType.INPUT, new ItemStack(Items.IRON_INGOT)));

        RecipeCraftingContext.beginSearchComponentMatchCache(ctrlA);
        try {
            RecipeCraftingContext ctx1 = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe1, 1), ctrlA);
            RecipeCraftingContext ctx2 = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe2, 1), ctrlA);
            assertTrue(ctx1.canStartCrafting().isSuccess());
            assertTrue(ctx2.canStartCrafting().isSuccess());

            // Must not leak ctrlA's cached components to ctrlB.
            RecipeCraftingContext ctxB = new RecipeCraftingContext(0, new ActiveMachineRecipe(recipe1, 1), ctrlB);
            assertTrue(ctxB.canStartCrafting().isFailure());

            // Multi-source batched modifier application preserves context permanent modifiers across overrideModifiers.
            RecipeModifier upgradeMod = new RecipeModifier(RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 0.5F, RecipeModifier.OPERATION_MULTIPLY, false);
            RecipeModifier customMod = new RecipeModifier(RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 0.5F, RecipeModifier.OPERATION_MULTIPLY, false);
            RecipeModifier ctxPerm = new RecipeModifier(RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, 0.5F, RecipeModifier.OPERATION_MULTIPLY, true);
            ctx1.addPermanentModifier(ctxPerm);
            ctx1.overrideModifiers(
                null,
                Collections.singletonList(upgradeMod),
                Collections.singletonList(customMod),
                null,
                null
            );

            assertEquals(25.0D, RecipeModifier.applyModifiers(ctx1, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, 100.0D, false), 1.0e-6D);
            assertEquals(0.5D, RecipeModifier.applyModifiers(ctx1, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, 1.0D, true), 1.0e-6D);
        } finally {
            RecipeCraftingContext.endSearchComponentMatchCache();
        }
    }

    private static class TestController extends TileMachineController {
        void group(long id, IItemHandlerModifiable items, FluidTank fluids) {
            Map<TileEntity, ProcessingComponent<?>> components = new HashMap<>();
            if (items != null) {
                MachineComponent.ItemBus bus = new MachineComponent.ItemBus(IOType.INPUT) {
                    @Override
                    public IItemHandlerModifiable getContainerProvider() {
                        return items;
                    }
                };
                components.put(new TileEntity() { }, new ProcessingComponent<>(bus, items, null));
            }
            if (fluids != null) {
                MachineComponent.FluidHatch hatch = new MachineComponent.FluidHatch(IOType.INPUT) {
                    @Override
                    public FluidTank getContainerProvider() {
                        return fluids;
                    }
                };
                components.put(new TileEntity() { }, new ProcessingComponent<>(hatch, fluids, null));
            }
            foundComponents.put(id, components);
        }

        @Override
        public void doControllerTick() {
        }

        @Override
        public void flushContextModifier() {
        }
    }

    private static class TestableSimpleUpgrade extends SimpleMachineUpgrade {
        TestableSimpleUpgrade(UpgradeType type) {
            super(type);
        }

        NBTTagCompound accessTag(NBTTagCompound current) {
            return captureCustomDataBeforeAccess(current);
        }

        void replaceTag(NBTTagCompound previous) {
            markCustomDataReplaced(previous);
        }

        boolean isDirty(NBTTagCompound current) {
            return isCustomDataDirty(current);
        }
    }
}
