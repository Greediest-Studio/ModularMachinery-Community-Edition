/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentBiome;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentDimension;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentDragonBreath;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentFlux;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentHeat;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentLaser;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentMeteor;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentPotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentRadiation;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentScrubber;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentVis;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentWillMultiChunk;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.*;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentEnergy;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentFluid;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentGas;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentItem;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentItemFluid;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentParallelController;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentParallelEqualizer;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentSmartInterface;
import hellfirepvp.modularmachinery.common.crafting.component.ComponentUpgradeBus;
import net.minecraft.util.ResourceLocation;

import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_ENERGY;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_FLUID;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_GAS;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_ITEM;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_PARALLEL_CONTROLLER;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_PARALLEL_EQUALIZER;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_SMART_INTERFACE;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.COMPONENT_UPGRADE_BUS;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_ENERGY;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_FLUID;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_GAS;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_ITEM;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_ITEM_FLUID;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_PARALLEL_CONTROLLER;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_PARALLEL_EQUALIZER;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_SMART_INTERFACE;
import static hellfirepvp.modularmachinery.common.lib.ComponentTypesMM.KEY_COMPONENT_UPGRADE_BUS;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryComponentTypes
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:51
 */
public class RegistryComponentTypes {

    private RegistryComponentTypes() {
    }

    public static void initialize() {
        COMPONENT_ITEM = register(new ComponentItem(), KEY_COMPONENT_ITEM);
        COMPONENT_FLUID = register(new ComponentFluid(), KEY_COMPONENT_FLUID);
        COMPONENT_ITEM_FLUID_GAS = register(new ComponentItemFluid(), KEY_COMPONENT_ITEM_FLUID);
        COMPONENT_ENERGY = register(new ComponentEnergy(), KEY_COMPONENT_ENERGY);
        COMPONENT_GAS = register(new ComponentGas(), KEY_COMPONENT_GAS);
        COMPONENT_SMART_INTERFACE = register(new ComponentSmartInterface(), KEY_COMPONENT_SMART_INTERFACE);
        COMPONENT_PARALLEL_CONTROLLER = register(new ComponentParallelController(), KEY_COMPONENT_PARALLEL_CONTROLLER);
        COMPONENT_PARALLEL_EQUALIZER = register(new ComponentParallelEqualizer(), KEY_COMPONENT_PARALLEL_EQUALIZER);
        COMPONENT_UPGRADE_BUS = register(new ComponentUpgradeBus(), KEY_COMPONENT_UPGRADE_BUS);

        register(new ComponentBiome(), KEY_COMPONENT_BIOME);
        register(new ComponentDimension(), KEY_COMPONENT_DIMENSION);

        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            register(new ComponentRadiation(), KEY_COMPONENT_RADIATION);
            register(new ComponentScrubber(), KEY_COMPONENT_SCRUBBER);
        }

        if (Mods.BM2.isPresent()) {
            register(new ComponentWillMultiChunk(), KEY_COMPONENT_WILL);
            register(new ComponentMeteor(), KEY_COMPONENT_METEOR);
        }

        if (Mods.TC6.isPresent()) {
            register(new ComponentFlux(), KEY_COMPONENT_FLUX);
            register(new ComponentVis(), KEY_COMPONENT_VIS);
        }

        if (Mods.ABYSSALCRAFT.isPresent()) {
            register(new ComponentPotentialEnergy(), KEY_COMPONENT_POTENTIAL_ENERGY);
        }

        if (Mods.ICE_AND_FIRE.isPresent()) {
            register(new ComponentDragonBreath(), KEY_COMPONENT_DRAGON_BREATH);
        }

        if (Mods.MEKANISM.isPresent()) {
            register(new ComponentLaser(), KEY_COMPONENT_LASER);
            register(new ComponentHeat(), KEY_COMPONENT_HEAT);
        }

    }

    private static <T extends ComponentType> T register(T componentType, ResourceLocation registryName) {
        componentType.setRegistryName(registryName);
        CommonProxy.registryPrimer.register(componentType);
        return componentType;
    }

}
