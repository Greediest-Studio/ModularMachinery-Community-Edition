package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineJEICategory;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.util.ControllerMode;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineModifier")
public class MachineModifier {
    public static final List<Action> WAIT_FOR_MODIFY = new LinkedList<>();

    @ZenMethod
    public static void addJEICategory(String machineName, String key) {
        addJEICategory(machineName, key, null, null, 0);
    }

    @ZenMethod
    public static void addJEICategory(String machineName, String key, String displayName) {
        addJEICategory(machineName, key, displayName, null, 0);
    }

    @ZenMethod
    public static void addJEICategory(String machineName, String key, IItemStack icon) {
        addJEICategory(machineName, key, null, icon, 0);
    }

    @ZenMethod
    public static void addJEICategory(String machineName, String key, String displayName, IItemStack icon) {
        addJEICategory(machineName, key, displayName, icon, 0);
    }

    @ZenMethod
    public static void addJEICategory(String machineName, String key, String displayName, IItemStack icon, int priority) {
        WAIT_FOR_MODIFY.add(() -> {
            ResourceLocation machineLoc = new ResourceLocation(machineName);
            if (machineLoc.getNamespace().equals("minecraft")) {
                machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getPath());
            }
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(machineLoc);
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            ItemStack iconStack = icon != null ? CraftTweakerMC.getItemStack(icon) : ItemStack.EMPTY;
            machine.addJEICategory(new MachineJEICategory(key, displayName, iconStack, priority));
        });
    }

    @ZenMethod
    public static void addJEICategory(String machineName, MachineJEICategory category) {
        WAIT_FOR_MODIFY.add(() -> {
            ResourceLocation machineLoc = new ResourceLocation(machineName);
            if (machineLoc.getNamespace().equals("minecraft")) {
                machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getPath());
            }
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(machineLoc);
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.addJEICategory(category);
        });
    }

    @ZenMethod
    public static void addControllerMode(String machineName, ControllerMode mode) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            if (!mode.getModes().containsKey(mode.getDefaultValue())) {
                CraftTweakerAPI.logError("ControllerMode `" + mode.getName() + "` must include its default value in addMode().");
                return;
            }
            if (machine.getControllerMode(mode.getName()) != null) {
                CraftTweakerAPI.logWarning("[ModularMachinery] DynamicMachine `" + machine.getRegistryName()
                    + "` already has ControllerMode `" + mode.getName() + "`!");
                return;
            }
            machine.addControllerMode(mode);
        });
    }

    @ZenMethod
    public static void addSmartInterfaceType(String machineName, SmartInterfaceType type) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            if (!machine.hasSmartInterfaceType(type.getType())) {
                machine.addSmartInterfaceType(type);
            } else {
                CraftTweakerAPI.logWarning("[ModularMachinery] DynamicMachine `" + machine.getRegistryName() + "` is already has SmartInterfaceType `" + type.getType() + "`!");
            }
        });
    }

    @ZenMethod
    public static void setMaxParallelism(String machineName, int maxParallelism) {
        if (maxParallelism < 1) {
            CraftTweakerAPI.logError("Max Parallelism must larger than 1!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setMaxParallelism(maxParallelism);
        });
    }

    @ZenMethod
    public static void setInternalParallelism(String machineName, int parallelism) {
        if (parallelism < 0) {
            CraftTweakerAPI.logError("Max Parallelism must larger than 0!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setInternalParallelism(parallelism);
        });
    }

    @ZenMethod
    public static void setMaxThreads(String machineName, int maxThreads) {
        // Maybe the author only wanted to use the core thread?
        if (maxThreads < 0) {
            CraftTweakerAPI.logError("Max Threads must larger than or equal 0!");
        }
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setMaxThreads(maxThreads);
        });
    }

    @ZenMethod
    public static void addCoreThread(String machineName, FactoryRecipeThread thread) {
        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.addCoreThread(thread);
        });
    }

    @ZenMethod
    public static void setMachineGeoModel(String machineName, String modelName) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }

        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }

            MachineControllerModel model = DynamicMachineModelRegistry.INSTANCE.getMachineModel(modelName);
            if (model == null) {
                CraftTweakerAPI.logError("Could not find geo model `" + modelName + "`!");
                return;
            }

            DynamicMachineModelRegistry.INSTANCE.registerMachineDefaultModel(machine, model);
        });
    }

    @ZenMethod
    public static void setMachinePrefix(String machineName, String prefixName) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }

        WAIT_FOR_MODIFY.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.setPrefix(prefixName);
        });
    }

    public static void loadAll() {
        for (Action waitForRegister : WAIT_FOR_MODIFY) {
            waitForRegister.doAction();
        }
        WAIT_FOR_MODIFY.clear();
    }
}
