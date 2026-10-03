/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeBiome;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeDimension;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeDragonBreath;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFlux;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeHeat;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeLaser;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeMeteor;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypePotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeRadiation;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeScrubber;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeVis;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeWillMultiChunk;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.*;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementDuration;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluidPerTick;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeGas;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeGasPerTick;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeInterfaceNumInput;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItem;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItemDurability;
import net.minecraft.util.ResourceLocation;

import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeCatalyst;

import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_INTERFACE_NUMBER_INPUT;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_CATALYST;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_DURATION;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_ENERGY;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_FLUID;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_FLUID_PERTICK;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_GAS;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_GAS_PERTICK;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_INGREDIENT_ARRAY;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_ITEM;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.KEY_REQUIREMENT_ITEM_DURABILITY;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_CATALYST;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_DURATION;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_ENERGY;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_FLUID;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_FLUID_PERTICK;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_GAS;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_GAS_PERTICK;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_INTERFACE_NUMBER_INPUT;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_ITEM;
import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.REQUIREMENT_ITEM_DURABILITY;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRequirementTypes
 * Created by HellFirePvP
 * Date: 13.07.2019 / 11:16
 */
public class RegistryRequirementTypes {

    private RegistryRequirementTypes() {
    }

    public static void initialize() {
        REQUIREMENT_ITEM = register(new RequirementTypeItem(), KEY_REQUIREMENT_ITEM);
        REQUIREMENT_ITEM_DURABILITY = register(new RequirementTypeItemDurability(), KEY_REQUIREMENT_ITEM_DURABILITY);
        REQUIREMENT_INGREDIENT_ARRAY = register(new RequirementTypeIngredientArray(), KEY_REQUIREMENT_INGREDIENT_ARRAY);
        REQUIREMENT_CATALYST = register(new RequirementTypeCatalyst(), KEY_REQUIREMENT_CATALYST);
        REQUIREMENT_FLUID = register(new RequirementTypeFluid(), KEY_REQUIREMENT_FLUID);
        REQUIREMENT_FLUID_PERTICK = register(new RequirementTypeFluidPerTick(), KEY_REQUIREMENT_FLUID_PERTICK);
        if (Mods.MEKANISM.isPresent()) {
            REQUIREMENT_GAS_PERTICK = register(new RequirementTypeGasPerTick(), KEY_REQUIREMENT_GAS_PERTICK);
        }
        REQUIREMENT_ENERGY = register(new RequirementTypeEnergy(), KEY_REQUIREMENT_ENERGY);
        if (Mods.MEKANISM.isPresent()) {
            REQUIREMENT_GAS = register(new RequirementTypeGas(), KEY_REQUIREMENT_GAS);
        }
        REQUIREMENT_INTERFACE_NUMBER_INPUT = register(new RequirementTypeInterfaceNumInput(), KEY_INTERFACE_NUMBER_INPUT);

        REQUIREMENT_DURATION = register(new RequirementDuration(), KEY_REQUIREMENT_DURATION);

        register(new RequirementTypeBiome(), KEY_REQUIREMENT_BIOME);
        register(new RequirementTypeDimension(), KEY_REQUIREMENT_DIMENSION);

        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            register(new RequirementTypeRadiation(), KEY_REQUIREMENT_RADIATION);
            register(new RequirementTypeScrubber(), KEY_REQUIREMENT_SCRUBBER);
        }

        if (Mods.BM2.isPresent()) {
            register(new RequirementTypeWillMultiChunk(), KEY_REQUIREMENT_WILL_MULTI_CHUNK);
            register(new RequirementTypeMeteor(), KEY_REQUIREMENT_METEOR);
        }

        if (Mods.TC6.isPresent()) {
            register(new RequirementTypeFlux(), KEY_REQUIREMENT_FLUX);
            register(new RequirementTypeVis(), KEY_REQUIREMENT_VIS);
        }

        if (Mods.ABYSSALCRAFT.isPresent()) {
            register(new RequirementTypePotentialEnergy(), KEY_REQUIREMENT_POTENTIAL_ENERGY);
        }

        if (Mods.ICE_AND_FIRE.isPresent()) {
            register(new RequirementTypeDragonBreath(), KEY_REQUIREMENT_DRAGON_BREATH);
        }

        if (Mods.MEKANISM.isPresent()) {
            register(new RequirementTypeLaser(), KEY_REQUIREMENT_LASER);
            register(new RequirementTypeHeat(), KEY_REQUIREMENT_HEAT);
        }

    }

    private static <T extends RequirementType<?, ?>> T register(T requirementType, ResourceLocation key) {
        requirementType.setRegistryName(key);
        CommonProxy.registryPrimer.register(requirementType);
        return requirementType;
    }

}
