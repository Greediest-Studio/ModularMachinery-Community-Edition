/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import hellfirepvp.modularmachinery.common.block.BlockBiomeProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockDimensionProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockDragonBreathInput;
import hellfirepvp.modularmachinery.common.block.BlockFluxProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockFluxProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockHeatInput;
import hellfirepvp.modularmachinery.common.block.BlockHeatOutput;
import hellfirepvp.modularmachinery.common.block.BlockLaserInput;
import hellfirepvp.modularmachinery.common.block.BlockMeteorProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockPotentialEnergyProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockPotentialEnergyProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockRadiationProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockRadiationProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockScrubberProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockVisProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockVisProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockWillMultiChunkProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockWillMultiChunkProviderOutput;

import hellfirepvp.modularmachinery.common.block.appeng.BlockMEFluidInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEFluidOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEGasInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEGasOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEItemInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEItemOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEPatternMirrorImage;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEPatternProvider;
import hellfirepvp.modularmachinery.common.block.BlockCasing;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockEnergyInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.block.BlockFluidInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFluidOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockInputBus;
import hellfirepvp.modularmachinery.common.block.BlockOutputBus;
import hellfirepvp.modularmachinery.common.block.BlockParallelEqualizerHatch;
import hellfirepvp.modularmachinery.common.block.BlockParallelController;
import hellfirepvp.modularmachinery.common.block.BlockSmartInterface;
import hellfirepvp.modularmachinery.common.block.BlockUpgradeBus;
import hellfirepvp.modularmachinery.common.block.BlockAspectProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockAspectProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockAuraProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockAuraProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockConstellationProvider;
import hellfirepvp.modularmachinery.common.block.BlockGridProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockGridProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockImpetusProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockImpetusProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockLifeEssenceProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockLifeEssenceProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockManaProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockManaProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockRainbowProvider;
import hellfirepvp.modularmachinery.common.block.BlockStarlightProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockStarlightProviderOutput;
import hellfirepvp.modularmachinery.common.block.BlockWillProviderInput;
import hellfirepvp.modularmachinery.common.block.BlockWillProviderOutput;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlocksMM
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class BlocksMM {

    public static BlockController        blockController;
    public static BlockFactoryController blockFactoryController;

    public static BlockCasing blockCasing;

    public static BlockInputBus          itemInputBus;
    public static BlockOutputBus         itemOutputBus;
    public static BlockFluidInputHatch   fluidInputHatch;
    public static BlockFluidOutputHatch  fluidOutputHatch;
    public static BlockEnergyInputHatch  energyInputHatch;
    public static BlockEnergyOutputHatch energyOutputHatch;

    public static BlockSmartInterface     smartInterface;
    public static BlockParallelController parallelController;
    public static BlockParallelEqualizerHatch parallelEqualizerHatch;
    public static BlockUpgradeBus         upgradeBus;

    public static BlockMEItemOutputBus      meItemOutputBus;
    public static BlockMEItemInputBus       meItemInputBus;
    public static BlockMEFluidOutputBus     meFluidOutputBus;
    public static BlockMEFluidInputBus      meFluidInputBus;
    public static BlockMEGasOutputBus       meGasOutputBus;
    public static BlockMEGasInputBus        meGasInputBus;
    public static BlockMEPatternProvider    mePatternProvider;
    public static BlockMEPatternMirrorImage mePatternMirrorImage;

    public static BlockWillProviderInput  blockWillProviderInput;
    public static BlockWillProviderOutput blockWillProviderOutput;

    public static BlockLifeEssenceProviderInput  blockLifeEssenceProviderInput;
    public static BlockLifeEssenceProviderOutput blockLifeEssenceProviderOutput;

    public static BlockGridProviderInput  blockGridProviderInput;
    public static BlockGridProviderOutput blockGridProviderOutput;

    public static BlockRainbowProvider blockRainbowProvider;

    public static BlockAuraProviderInput  blockAuraProviderInput;
    public static BlockAuraProviderOutput blockAuraProviderOutput;

    public static BlockStarlightProviderInput  blockStarlightProviderInput;
    public static BlockStarlightProviderOutput blockStarlightProviderOutput;

    public static BlockConstellationProvider blockConstellationProvider;

    public static BlockManaProviderInput  blockManaProviderInput;
    public static BlockManaProviderOutput blockManaProviderOutput;

    public static BlockImpetusProviderInput  blockImpetusProviderInput;
    public static BlockImpetusProviderOutput blockImpetusProviderOutput;

    public static BlockAspectProviderInput  blockAspectProviderInput;
    public static BlockAspectProviderOutput blockAspectProviderOutput;

    public static BlockBiomeProviderInput blockBiomeProviderInput;
    public static BlockDimensionProviderInput blockDimensionProviderInput;
    public static BlockRadiationProviderInput blockRadiationProviderInput;
    public static BlockRadiationProviderOutput blockRadiationProviderOutput;
    public static BlockScrubberProviderInput blockScrubberProviderInput;
    public static BlockWillMultiChunkProviderInput blockWillMultiChunkProviderInput;
    public static BlockWillMultiChunkProviderOutput blockWillMultiChunkProviderOutput;
    public static BlockMeteorProviderOutput blockMeteorProviderOutput;
    public static BlockFluxProviderInput blockFluxProviderInput;
    public static BlockFluxProviderOutput blockFluxProviderOutput;
    public static BlockVisProviderInput blockVisProviderInput;
    public static BlockVisProviderOutput blockVisProviderOutput;
    public static BlockPotentialEnergyProviderInput blockPotentialEnergyProviderInput;
    public static BlockPotentialEnergyProviderOutput blockPotentialEnergyProviderOutput;
    public static BlockDragonBreathInput blockDragonBreathProviderInput;
    public static BlockLaserInput blockLaserProviderInput;
    public static BlockHeatInput blockHeatProviderInput;
    public static BlockHeatOutput blockHeatProviderOutput;

}
