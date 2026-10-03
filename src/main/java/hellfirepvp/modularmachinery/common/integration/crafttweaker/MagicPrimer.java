package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementBiome;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementDimension;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementDragonBreath;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFlux;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementHeat;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementLaser;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementMeteor;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementPotentialEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementRadiation;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementScrubber;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementVis;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementWillMultiChunk;
import hellfirepvp.modularmachinery.common.integration.iceandfire.DragonType;
import hellfirepvp.modularmachinery.common.tiles.TileFluxProvider;
import hellfirepvp.modularmachinery.common.tiles.TileVisProvider;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementAura;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementConstellation;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGrid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementImpetus;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementLifeEssence;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementMana;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementRainbow;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementStarlight;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementWill;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Aura;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenExpansion("mods.modularmachinery.RecipePrimer")
public class MagicPrimer {

    @ZenMethod
    public static RecipePrimer addAuraInput(RecipePrimer primer, String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            primer.appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        } else {
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraOutput(RecipePrimer primer, String auraType, int amount) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            primer.appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), Integer.MAX_VALUE, Integer.MIN_VALUE));
        } else {
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraInput(RecipePrimer primer, String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            primer.appendComponent(new RequirementAura(IOType.INPUT, new Aura(amount, aura), max, min));
        } else {
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addAuraOutput(RecipePrimer primer, String auraType, int amount, int max, int min) {
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation("naturesaura", auraType));
        if (aura != null) {
            primer.appendComponent(new RequirementAura(IOType.OUTPUT, new Aura(amount, aura), max, min));
        } else {
            CraftTweakerAPI.logError("Invalid aura name : " + auraType);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addConstellationInput(RecipePrimer primer, String constellationString) {
        IConstellation constellation = ConstellationRegistry.getConstellationByName("astralsorcery.constellation." + constellationString);
        if (constellation != null) {
            primer.appendComponent(new RequirementConstellation(IOType.INPUT, constellation));
        } else {
            CraftTweakerAPI.logError("Invalid constellation : " + constellationString);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addGridPowerInput(RecipePrimer primer, int amount) {
        if (amount > 0) {
            primer.appendComponent(new RequirementGrid(IOType.INPUT, amount));
        } else {
            CraftTweakerAPI.logError("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addGridPowerOutput(RecipePrimer primer, int amount) {
        if (amount > 0) {
            primer.appendComponent(new RequirementGrid(IOType.OUTPUT, amount));
        } else {
            CraftTweakerAPI.logError("Invalid Grid Power amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addRainbowInput(RecipePrimer primer) {
        primer.appendComponent(new RequirementRainbow());
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addLifeEssenceInput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0) {
            primer.appendComponent(new RequirementLifeEssence(IOType.INPUT, amount, perTick));
        } else {
            CraftTweakerAPI.logError("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addLifeEssenceOutput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0) {
            primer.appendComponent(new RequirementLifeEssence(IOType.OUTPUT, amount, perTick));
        } else {
            CraftTweakerAPI.logError("Invalid Life Essence amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addStarlightInput(RecipePrimer primer, float amount) {
        if (amount > 0) {
            primer.appendComponent(new RequirementStarlight(IOType.INPUT, amount));
        } else {
            CraftTweakerAPI.logError("Invalid Starlight amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addStarlightOutput(RecipePrimer primer, float amount) {
        if (amount > 0) {
            primer.appendComponent(new RequirementStarlight(IOType.OUTPUT, amount));
        } else {
            CraftTweakerAPI.logError("Invalid Starlight amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillInput(RecipePrimer primer, String willTypeString, int amount) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null) {
            primer.appendComponent(new RequirementWill(IOType.INPUT, amount, willType, 0, Integer.MAX_VALUE));
        } else {
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillOutput(RecipePrimer primer, String willTypeString, int amount) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null) {
            primer.appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, 0, Integer.MAX_VALUE));
        } else {
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillInput(RecipePrimer primer, String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null) {
            primer.appendComponent(new RequirementWill(IOType.INPUT, amount, willType, min, max));
        } else {
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillOutput(RecipePrimer primer, String willTypeString, int amount, int min, int max) {
        EnumDemonWillType willType = EnumDemonWillType.valueOf(willTypeString);
        if (willType != null) {
            primer.appendComponent(new RequirementWill(IOType.OUTPUT, amount, willType, min, max));
        } else {
            CraftTweakerAPI.logError("Invalid demon will type : " + willTypeString);
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addManaInput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0) {
            primer.appendComponent(new RequirementMana(IOType.INPUT, amount, perTick));
        } else {
            CraftTweakerAPI.logError("Invalid Mana amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addManaOutput(RecipePrimer primer, int amount, boolean perTick) {
        if (amount > 0) {
            primer.appendComponent(new RequirementMana(IOType.OUTPUT, amount, perTick));
        } else {
            CraftTweakerAPI.logError("Invalid Mana amount : " + amount + " (need to be positive and not null)");
        }

        return primer;
    }

    @ZenMethod
    public static RecipePrimer addImpetusInput(RecipePrimer primer, int amount) {
        primer.appendComponent(new RequirementImpetus(IOType.INPUT, amount));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addImpetusOutput(RecipePrimer primer, int amount) {
        primer.appendComponent(new RequirementImpetus(IOType.OUTPUT, amount));
        return primer;
    }

    @FunctionalInterface
    private interface RequirementSupplier<T extends ComponentRequirement<?, ?>> {
        T get() throws RequirementPrerequisiteFailedException;
    }

    private static <T extends ComponentRequirement<?, ?>> RecipePrimer addRequirement(RecipePrimer primer, RequirementSupplier<T> supplier) {
        ComponentRequirement<?, ?> requirement = supplier.get();
        if (requirement.getRequirementType() == null) {
            throw new RequirementPrerequisiteFailedException("Required integration mod is not loaded: " + requirement.getClass().getSimpleName());
        }
        primer.appendComponent(requirement);
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkInput(RecipePrimer primer, int chunkRange, int amount, int minPerChunk, int maxPerChunk, String willType) {
        return addRequirement(primer, () -> RequirementWillMultiChunk.from(IOType.INPUT, chunkRange, amount, minPerChunk, maxPerChunk, willType));
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkInput(RecipePrimer primer, int amount, int minPerChunk, int maxPerChunk, String willType) {
        return addWillMultiChunkInput(primer, 0, amount, minPerChunk, maxPerChunk, willType);
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkInput(RecipePrimer primer, int amount, String willType) {
        return addWillMultiChunkInput(primer, 0, amount, 0, Integer.MAX_VALUE, willType);
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkOutput(RecipePrimer primer, int chunkRange, int amount, int minPerChunk, int maxPerChunk, String willType) {
        return addRequirement(primer, () -> RequirementWillMultiChunk.from(IOType.OUTPUT, chunkRange, amount, minPerChunk, maxPerChunk, willType));
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkOutput(RecipePrimer primer, int amount, int minPerChunk, int maxPerChunk, String willType) {
        return addWillMultiChunkOutput(primer, 0, amount, minPerChunk, maxPerChunk, willType);
    }

    @ZenMethod
    public static RecipePrimer addWillMultiChunkOutput(RecipePrimer primer, int amount, String willType) {
        return addWillMultiChunkOutput(primer, 0, amount, 0, Integer.MAX_VALUE, willType);
    }

    @ZenMethod
    public static RecipePrimer addFluxInput(RecipePrimer primer, float amount, int chunkRange) {
        return addRequirement(primer, () -> RequirementFlux.from(IOType.INPUT, chunkRange, amount));
    }

    @ZenMethod
    public static RecipePrimer addFluxInput(RecipePrimer primer, float amount, int chunkRange, int minPerChunk) {
        return addRequirement(primer, () -> RequirementFlux.from(IOType.INPUT, chunkRange, amount, minPerChunk, TileFluxProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));
    }

    @ZenMethod
    public static RecipePrimer addFluxInput(RecipePrimer primer, float amount) {
        return addFluxInput(primer, amount, 0);
    }

    @ZenMethod
    public static RecipePrimer addFluxOutput(RecipePrimer primer, float amount, int chunkRange) {
        return addRequirement(primer, () -> RequirementFlux.from(IOType.OUTPUT, chunkRange, amount));
    }

    @ZenMethod
    public static RecipePrimer addFluxOutput(RecipePrimer primer, float amount, int chunkRange, int maxPerChunk) {
        return addRequirement(primer, () -> RequirementFlux.from(IOType.OUTPUT, chunkRange, amount, 0, maxPerChunk));
    }

    @ZenMethod
    public static RecipePrimer addFluxOutput(RecipePrimer primer, float amount) {
        return addFluxOutput(primer, amount, 0);
    }

    @ZenMethod
    public static RecipePrimer addVisInput(RecipePrimer primer, float amount, int chunkRange) {
        return addRequirement(primer, () -> RequirementVis.from(IOType.INPUT, chunkRange, amount));
    }

    @ZenMethod
    public static RecipePrimer addVisInput(RecipePrimer primer, float amount, int chunkRange, int minPerChunk) {
        return addRequirement(primer, () -> RequirementVis.from(IOType.INPUT, chunkRange, amount, minPerChunk, TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK));
    }

    @ZenMethod
    public static RecipePrimer addVisInput(RecipePrimer primer, float amount) {
        return addVisInput(primer, amount, 0);
    }

    @ZenMethod
    public static RecipePrimer addVisOutput(RecipePrimer primer, float amount, int chunkRange) {
        return addRequirement(primer, () -> RequirementVis.from(IOType.OUTPUT, chunkRange, amount));
    }

    @ZenMethod
    public static RecipePrimer addVisOutput(RecipePrimer primer, float amount, int chunkRange, int maxPerChunk) {
        return addRequirement(primer, () -> RequirementVis.from(IOType.OUTPUT, chunkRange, amount, 0, maxPerChunk));
    }

    @ZenMethod
    public static RecipePrimer addVisOutput(RecipePrimer primer, float amount) {
        return addVisOutput(primer, amount, 0);
    }

    @ZenMethod
    public static RecipePrimer addPotentialEnergyInput(RecipePrimer primer, float amount) {
        return addRequirement(primer, () -> RequirementPotentialEnergy.from(IOType.INPUT, amount));
    }

    @ZenMethod
    public static RecipePrimer addPotentialEnergyOutput(RecipePrimer primer, float amount) {
        return addRequirement(primer, () -> RequirementPotentialEnergy.from(IOType.OUTPUT, amount));
    }

    @ZenMethod
    public static RecipePrimer addFireDragonBreathInput(RecipePrimer primer, int amount) {
        return addRequirement(primer, () -> RequirementDragonBreath.from(IOType.INPUT, DragonType.FIRE.name(), amount));
    }

    @ZenMethod
    public static RecipePrimer addIceDragonBreathInput(RecipePrimer primer, int amount) {
        return addRequirement(primer, () -> RequirementDragonBreath.from(IOType.INPUT, DragonType.ICE.name(), amount));
    }

    @ZenMethod
    public static RecipePrimer addLightningDragonBreathInput(RecipePrimer primer, int amount) {
        return addRequirement(primer, () -> RequirementDragonBreath.from(IOType.INPUT, DragonType.LIGHTNING.name(), amount));
    }

    @ZenMethod
    public static RecipePrimer addMeteorOutput(RecipePrimer primer, String catalystItem) {
        return addRequirement(primer, () -> RequirementMeteor.from(catalystItem));
    }

}
