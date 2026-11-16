package github.kasuminova.mmce.client;

import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ControllerGeoModelTest {
    private static final List<Supplier<TileMultiblockMachineController>> CONTROLLERS =
        Arrays.asList(TileMachineController::new, TileFactoryController::new);

    @BeforeClass
    public static void initMinecraft() {
        Bootstrap.register();
    }

    @Test
    public void oldSavesKeepModelsEnabledForBothControllerTypes() {
        for (Supplier<TileMultiblockMachineController> factory : CONTROLLERS) {
            TileMultiblockMachineController controller = factory.get();
            assertTrue(controller.isGeoModelEnabled());
            controller.setGeoModelEnabled(false);
            controller.readCustomNBT(new NBTTagCompound());
            assertTrue(controller.isGeoModelEnabled());
        }
    }

    @Test
    public void visibilitySurvivesSavingAndNbtSyncWithoutFormedStructure() {
        for (Supplier<TileMultiblockMachineController> factory : CONTROLLERS) {
            TileMultiblockMachineController controller = factory.get();
            assertFalse(controller.isStructureFormed());
            controller.setGeoModelEnabled(false);

            // 存档与方块实体同步共用 writeCustomNBT/readCustomNBT。
            NBTTagCompound data = new NBTTagCompound();
            controller.writeCustomNBT(data);
            TileMultiblockMachineController restored = factory.get();
            restored.readCustomNBT(data);
            assertFalse(restored.isGeoModelEnabled());

            restored.setGeoModelEnabled(true);
            restored.writeCustomNBT(data);
            controller.readCustomNBT(data);
            assertTrue(controller.isGeoModelEnabled());
        }
    }

    @Test
    public void hiddenModelRemainsConfigurableAndCanBeEnabledAgain() {
        TestController controller = new TestController();
        DynamicMachine machine = new DynamicMachine("geo_toggle_test");
        MachineControllerModel model = model("geo_toggle_test");
        controller.foundMachine(machine);
        assertFalse(controller.hasGeoModel());
        DynamicMachineModelRegistry.INSTANCE.registerMachineDefaultModel(machine, model);
        assertSame(model, controller.getCurrentModel());

        controller.setGeoModelEnabled(false);
        assertNull(controller.getCurrentModel());
        assertSame(model, controller.getConfiguredModel());
        assertTrue(controller.hasGeoModel());
        assertSame(machine, controller.getFoundMachine());
        assertEquals(1, controller.updates);

        controller.setGeoModelEnabled(false);
        assertEquals(1, controller.updates);
        controller.setGeoModelEnabled(true);
        assertSame(model, controller.getCurrentModel());
        assertEquals(2, controller.updates);
    }

    @Test
    public void unformedDedicatedControllerStillOffersModelSetting() {
        TestController controller = new TestController();
        DynamicMachine machine = new DynamicMachine("geo_dedicated_test");
        controller.parentMachine(machine);
        assertFalse(controller.hasGeoModel());
        DynamicMachineModelRegistry.INSTANCE.registerMachineDefaultModel(machine, model("geo_dedicated_test"));
        assertFalse(controller.isStructureFormed());
        assertTrue(controller.hasGeoModel());
        controller.setGeoModelEnabled(false);
        assertTrue(controller.hasGeoModel());
    }

    private static MachineControllerModel model(String name) {
        ResourceLocation resource = new ResourceLocation("modularmachinery", name);
        return new MachineControllerModel(name, resource, resource, resource);
    }

    private static class TestController extends TileMachineController {
        private int updates;

        private void foundMachine(DynamicMachine machine) {
            foundMachine = machine;
        }

        private void parentMachine(DynamicMachine machine) {
            parentMachine = machine;
        }

        @Override
        public String getCurrentModelName() {
            return "";
        }

        @Override
        public void markForUpdate() {
            updates++;
        }
    }
}
