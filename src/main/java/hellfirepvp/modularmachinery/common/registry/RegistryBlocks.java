/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

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
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.tiles.TileBiomeProvider;
import hellfirepvp.modularmachinery.common.tiles.TileDimensionProvider;
import hellfirepvp.modularmachinery.common.tiles.TileDragonBreathProvider;
import hellfirepvp.modularmachinery.common.tiles.TileFluxProvider;
import hellfirepvp.modularmachinery.common.tiles.TileHeatProvider;
import hellfirepvp.modularmachinery.common.tiles.TileLaserProvider;
import hellfirepvp.modularmachinery.common.tiles.TileMeteorProvider;
import hellfirepvp.modularmachinery.common.tiles.TilePotentialEnergyProvider;
import hellfirepvp.modularmachinery.common.tiles.TileRadiationProvider;
import hellfirepvp.modularmachinery.common.tiles.TileScrubberProvider;
import hellfirepvp.modularmachinery.common.tiles.TileVisProvider;
import hellfirepvp.modularmachinery.common.tiles.TileWillMultiChunkProvider;

import hellfirepvp.modularmachinery.common.block.appeng.BlockMEFluidInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEFluidOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEGasInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEGasOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEItemInputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEItemOutputBus;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEMachineComponent;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEPatternMirrorImage;
import hellfirepvp.modularmachinery.common.block.appeng.BlockMEPatternProvider;
import hellfirepvp.modularmachinery.common.tiles.MEFluidInputBus;
import hellfirepvp.modularmachinery.common.tiles.MEFluidOutputBus;
import hellfirepvp.modularmachinery.common.tiles.MEGasInputBus;
import hellfirepvp.modularmachinery.common.tiles.MEGasOutputBus;
import hellfirepvp.modularmachinery.common.tiles.MEItemInputBus;
import hellfirepvp.modularmachinery.common.tiles.MEItemOutputBus;
import hellfirepvp.modularmachinery.common.tiles.MEPatternMirrorImage;
import hellfirepvp.modularmachinery.common.tiles.MEPatternProvider;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockCasing;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockCustomName;
import hellfirepvp.modularmachinery.common.block.BlockDynamicColor;
import hellfirepvp.modularmachinery.common.block.BlockEnergyInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.block.BlockFluidInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFluidOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockInputBus;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import hellfirepvp.modularmachinery.common.block.BlockOutputBus;
import hellfirepvp.modularmachinery.common.block.BlockParallelEqualizerHatch;
import hellfirepvp.modularmachinery.common.block.BlockParallelController;
import hellfirepvp.modularmachinery.common.block.BlockSmartInterface;
import hellfirepvp.modularmachinery.common.block.BlockStatedMachineComponent;
import hellfirepvp.modularmachinery.common.block.BlockUpgradeBus;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import hellfirepvp.modularmachinery.common.item.ItemBlockCustomName;
import hellfirepvp.modularmachinery.common.item.ItemBlockMEMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponentCustomName;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyInputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.TileFluidInputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileFluidOutputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileItemInputBus;
import hellfirepvp.modularmachinery.common.tiles.TileItemOutputBus;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.TileParallelController;
import hellfirepvp.modularmachinery.common.tiles.TileParallelEqualizerHatch;
import hellfirepvp.modularmachinery.common.tiles.TileSmartInterface;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
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
import hellfirepvp.modularmachinery.common.tiles.TileAspectProvider;
import hellfirepvp.modularmachinery.common.tiles.TileAuraProvider;
import hellfirepvp.modularmachinery.common.tiles.TileConstellationProvider;
import hellfirepvp.modularmachinery.common.tiles.TileGridProvider;
import hellfirepvp.modularmachinery.common.tiles.TileImpetusComponent;
import hellfirepvp.modularmachinery.common.tiles.TileLifeEssenceProvider;
import hellfirepvp.modularmachinery.common.tiles.TileManaProvider;
import hellfirepvp.modularmachinery.common.tiles.TileRainbowProvider;
import hellfirepvp.modularmachinery.common.tiles.TileStarlightInput;
import hellfirepvp.modularmachinery.common.tiles.TileStarlightOutput;
import hellfirepvp.modularmachinery.common.tiles.TileWillProvider;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockAspectProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockAspectProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockAuraProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockAuraProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockCasing;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockConstellationProvider;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockController;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockFactoryController;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockGridProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockGridProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockImpetusProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockImpetusProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockLifeEssenceProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockLifeEssenceProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockManaProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockManaProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockRainbowProvider;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockStarlightProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockStarlightProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockWillProviderInput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.blockWillProviderOutput;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.energyInputHatch;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.energyOutputHatch;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.fluidInputHatch;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.fluidOutputHatch;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.itemInputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.itemOutputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meFluidInputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meFluidOutputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meGasInputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meGasOutputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meItemInputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.meItemOutputBus;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.mePatternMirrorImage;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.mePatternProvider;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.parallelController;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.parallelEqualizerHatch;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.smartInterface;
import static hellfirepvp.modularmachinery.common.lib.BlocksMM.upgradeBus;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlocks
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class RegistryBlocks {

    public static final  List<BlockDynamicColor> pendingIBlockColorBlocks = new LinkedList<>();
    private static final List<Block>             blockModelRegister       = new ArrayList<>();

    public static void initialize() {
        registerBlocks();
        if (Mods.RESOURCELOADER.isPresent() || Mods.TX_LOADER.isPresent()) {
            try {
                RegistryBlocks.writeAllCustomControllerModels();
            } catch (IOException e) {
                ModularMachinery.log.error("Failed to write controller models", e);
            }
        }

        registerTiles();
        registerBlockModels();
    }

    private static void registerBlocks() {
        blockController = prepareRegister(new BlockController());
        prepareItemBlockRegister(new ItemBlockController(blockController));
        blockFactoryController = prepareRegister(new BlockFactoryController());
        prepareItemBlockRegister(blockFactoryController);

        registerCustomControllers();

        registerExampleStatedMachineComponent();
        registerCustomStatedMachineComponent();

        blockCasing = prepareRegister(new BlockCasing());
        prepareItemBlockRegister(blockCasing);

        itemInputBus = prepareRegister(new BlockInputBus());
        prepareItemBlockRegister(itemInputBus);
        itemOutputBus = prepareRegister(new BlockOutputBus());
        prepareItemBlockRegister(itemOutputBus);
        fluidInputHatch = prepareRegister(new BlockFluidInputHatch());
        prepareItemBlockRegister(fluidInputHatch);
        fluidOutputHatch = prepareRegister(new BlockFluidOutputHatch());
        prepareItemBlockRegister(fluidOutputHatch);
        energyInputHatch = prepareRegister(new BlockEnergyInputHatch());
        prepareItemBlockRegister(energyInputHatch);
        energyOutputHatch = prepareRegister(new BlockEnergyOutputHatch());
        prepareItemBlockRegister(energyOutputHatch);
        smartInterface = prepareRegister(new BlockSmartInterface());
        prepareItemBlockRegister(smartInterface);
        parallelController = prepareRegister(new BlockParallelController());
        prepareItemBlockRegister(parallelController);

        parallelEqualizerHatch = new BlockParallelEqualizerHatch();
        parallelEqualizerHatch.setRegistryName(ModularMachinery.MODID, BlockParallelEqualizerHatch.NAME);
        parallelEqualizerHatch.setTranslationKey(ModularMachinery.MODID + '.' + BlockParallelEqualizerHatch.NAME);
        prepareRegisterWithCustomName(parallelEqualizerHatch);

        ItemBlock parallelEqualizerHatchItem = new ItemBlock(parallelEqualizerHatch);
        parallelEqualizerHatchItem.setRegistryName(parallelEqualizerHatch.getRegistryName());
        parallelEqualizerHatchItem.setTranslationKey(ModularMachinery.MODID + '.' + BlockParallelEqualizerHatch.NAME);
        prepareItemBlockRegisterWithCustomName(parallelEqualizerHatchItem);

        upgradeBus = prepareRegister(new BlockUpgradeBus());
        prepareItemBlockRegister(upgradeBus);

        if (Mods.AE2.isPresent()) {
            meItemOutputBus = prepareRegister(new BlockMEItemOutputBus());
            ItemsMM.meItemOutputBus = prepareItemBlockRegister(meItemOutputBus);
            meItemInputBus = prepareRegister(new BlockMEItemInputBus());
            ItemsMM.meItemInputBus = prepareItemBlockRegister(meItemInputBus);

            meFluidOutputBus = prepareRegister(new BlockMEFluidOutputBus());
            ItemsMM.meFluidOutputBus = prepareItemBlockRegister(meFluidOutputBus);
            meFluidInputBus = prepareRegister(new BlockMEFluidInputBus());
            ItemsMM.meFluidInputBus = prepareItemBlockRegister(meFluidInputBus);

            if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
                meGasOutputBus = prepareRegister(new BlockMEGasOutputBus());
                ItemsMM.meGasOutputBus = prepareItemBlockRegister(meGasOutputBus);
                meGasInputBus = prepareRegister(new BlockMEGasInputBus());
                ItemsMM.meGasInputBus = prepareItemBlockRegister(meGasInputBus);
            }

            mePatternProvider = prepareRegister(new BlockMEPatternProvider());
            ItemsMM.mePatternProvider = prepareItemBlockRegister(mePatternProvider);
            mePatternMirrorImage = prepareRegister(new BlockMEPatternMirrorImage());
            ItemsMM.mePatternMirrorImage = prepareItemBlockRegister(mePatternMirrorImage);
        }

        if (Mods.BM2.isPresent()) {
            blockWillProviderInput = prepareRegister(new BlockWillProviderInput());
            blockWillProviderOutput = prepareRegister(new BlockWillProviderOutput());
            blockLifeEssenceProviderInput = prepareRegister(new BlockLifeEssenceProviderInput());
            blockLifeEssenceProviderOutput = prepareRegister(new BlockLifeEssenceProviderOutput());

            prepareItemBlockRegister(blockWillProviderInput);
            prepareItemBlockRegister(blockWillProviderOutput);
            prepareItemBlockRegister(blockLifeEssenceProviderInput);
            prepareItemBlockRegister(blockLifeEssenceProviderOutput);
        }

        if (Mods.TC6.isPresent()) {
            blockAspectProviderInput = prepareRegister(new BlockAspectProviderInput());
            blockAspectProviderOutput = prepareRegister(new BlockAspectProviderOutput());

            prepareItemBlockRegister(blockAspectProviderInput);
            prepareItemBlockRegister(blockAspectProviderOutput);
        }

        if (Mods.TA.isPresent()) {
            blockImpetusProviderInput = prepareRegister(new BlockImpetusProviderInput());
            blockImpetusProviderOutput = prepareRegister(new BlockImpetusProviderOutput());

            prepareItemBlockRegister(blockImpetusProviderInput);
            prepareItemBlockRegister(blockImpetusProviderOutput);
        }

        if (Mods.EXU2.isPresent()) {
            blockGridProviderInput = prepareRegister(new BlockGridProviderInput());
            blockGridProviderOutput = prepareRegister(new BlockGridProviderOutput());
            blockRainbowProvider = prepareRegister(new BlockRainbowProvider());

            prepareItemBlockRegister(blockGridProviderInput);
            prepareItemBlockRegister(blockGridProviderOutput);
            prepareItemBlockRegister(blockRainbowProvider);
        }

        if (Mods.ASTRAL_SORCERY.isPresent()) {
            blockStarlightProviderInput = prepareRegister(new BlockStarlightProviderInput());
            blockStarlightProviderOutput = prepareRegister(new BlockStarlightProviderOutput());
            blockConstellationProvider = prepareRegister(new BlockConstellationProvider());

            prepareItemBlockRegister(blockStarlightProviderInput);
            prepareItemBlockRegister(blockStarlightProviderOutput);
            prepareItemBlockRegister(blockConstellationProvider);
        }

        if (Mods.NATURESAURA.isPresent()) {
            blockAuraProviderInput = prepareRegister(new BlockAuraProviderInput());
            blockAuraProviderOutput = prepareRegister(new BlockAuraProviderOutput());

            prepareItemBlockRegister(blockAuraProviderInput);
            prepareItemBlockRegister(blockAuraProviderOutput);
        }

        if (Mods.BOTANIA.isPresent()) {
            blockManaProviderInput = prepareRegister(new BlockManaProviderInput());
            blockManaProviderOutput = prepareRegister(new BlockManaProviderOutput());

            prepareItemBlockRegister(blockManaProviderInput);
            prepareItemBlockRegister(blockManaProviderOutput);
        }

        // Biome and Dimension: always registered
        BlocksMM.blockBiomeProviderInput = prepareIntegrationBlock(new BlockBiomeProviderInput(), "blockbiomeproviderinput");
        BlocksMM.blockDimensionProviderInput = prepareIntegrationBlock(new BlockDimensionProviderInput(), "blockdimensionproviderinput");

        // NuclearCraft-related blocks
        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            BlocksMM.blockRadiationProviderInput = prepareIntegrationBlock(new BlockRadiationProviderInput(), "blockradiationproviderinput");
            BlocksMM.blockRadiationProviderOutput = prepareIntegrationBlock(new BlockRadiationProviderOutput(), "blockradiationprovideroutput");
            BlocksMM.blockScrubberProviderInput = prepareIntegrationBlock(new BlockScrubberProviderInput(), "blockscrubberproviderinput");

            prepareItemBlockRegister(BlocksMM.blockRadiationProviderInput);
            prepareItemBlockRegister(BlocksMM.blockRadiationProviderOutput);
            prepareItemBlockRegister(BlocksMM.blockScrubberProviderInput);
        }

        // Blood Magic (Will & meteor)
        if (Mods.BM2.isPresent()) {
            BlocksMM.blockWillMultiChunkProviderInput = prepareIntegrationBlock(new BlockWillMultiChunkProviderInput(), "blockwillmultichunkproviderinput");
            BlocksMM.blockWillMultiChunkProviderOutput = prepareIntegrationBlock(new BlockWillMultiChunkProviderOutput(), "blockwillmultichunkprovideroutput");
            BlocksMM.blockMeteorProviderOutput = prepareIntegrationBlock(new BlockMeteorProviderOutput(), "blockmeteorprovideroutput");

            prepareItemBlockRegister(BlocksMM.blockWillMultiChunkProviderInput);
            prepareItemBlockRegister(BlocksMM.blockWillMultiChunkProviderOutput);
            prepareItemBlockRegister(BlocksMM.blockMeteorProviderOutput);
        }

        // Thaumcraft (Flux & Vis)
        if (Mods.TC6.isPresent()) {
            BlocksMM.blockFluxProviderInput = prepareIntegrationBlock(new BlockFluxProviderInput(), "blockfluxproviderinput");
            BlocksMM.blockFluxProviderOutput = prepareIntegrationBlock(new BlockFluxProviderOutput(), "blockfluxprovideroutput");
            BlocksMM.blockVisProviderInput = prepareIntegrationBlock(new BlockVisProviderInput(), "blockvisproviderinput");
            BlocksMM.blockVisProviderOutput = prepareIntegrationBlock(new BlockVisProviderOutput(), "blockvisprovideroutput");

            prepareItemBlockRegister(BlocksMM.blockFluxProviderInput);
            prepareItemBlockRegister(BlocksMM.blockFluxProviderOutput);
            prepareItemBlockRegister(BlocksMM.blockVisProviderInput);
            prepareItemBlockRegister(BlocksMM.blockVisProviderOutput);
        }

        if (Mods.ABYSSALCRAFT.isPresent()) {
            BlocksMM.blockPotentialEnergyProviderInput = prepareIntegrationBlock(new BlockPotentialEnergyProviderInput(), "blockpotentialenergyproviderinput");
            BlocksMM.blockPotentialEnergyProviderOutput = prepareIntegrationBlock(new BlockPotentialEnergyProviderOutput(), "blockpotentialenergyprovideroutput");

            prepareItemBlockRegister(BlocksMM.blockPotentialEnergyProviderInput);
            prepareItemBlockRegister(BlocksMM.blockPotentialEnergyProviderOutput);
        }

        if (Mods.ICE_AND_FIRE.isPresent()) {
            BlocksMM.blockDragonBreathProviderInput = prepareIntegrationBlock(new BlockDragonBreathInput(), "blockdragonbreathinput");

            prepareItemBlockRegister(BlocksMM.blockDragonBreathProviderInput);
        }

        if (Mods.MEKANISM.isPresent()) {
            BlocksMM.blockLaserProviderInput = prepareIntegrationBlock(new BlockLaserInput(), "blocklaserinput");
            BlocksMM.blockHeatProviderInput = prepareIntegrationBlock(new BlockHeatInput(), "blockheatinput");
            BlocksMM.blockHeatProviderOutput = prepareIntegrationBlock(new BlockHeatOutput(), "blockheatoutput");

            prepareItemBlockRegister(BlocksMM.blockLaserProviderInput);
            prepareItemBlockRegister(BlocksMM.blockHeatProviderInput);
            prepareItemBlockRegister(BlocksMM.blockHeatProviderOutput);
        }

        // Always register
        prepareItemBlockRegister(BlocksMM.blockBiomeProviderInput);
        prepareItemBlockRegister(BlocksMM.blockDimensionProviderInput);

    }

    private static void registerTiles() {
        registerTile(TileColorableMachineComponent.class);

        registerTile(TileMachineController.class);
        registerTile(TileFactoryController.class);

        registerTile(TileFluidInputHatch.class);
        registerTile(TileFluidOutputHatch.class);
        registerTile(TileItemOutputBus.class);
        registerTile(TileItemInputBus.class);
        registerTile(TileEnergyInputHatch.class);
        registerTile(TileEnergyOutputHatch.class);

        registerTile(TileSmartInterface.class);
        registerTile(TileParallelController.class);
        registerTile(TileParallelEqualizerHatch.class);
        registerTile(TileUpgradeBus.class);

        if (Mods.AE2.isPresent()) {
            registerTileWithModID(MEItemOutputBus.class);
            registerTileWithModID(MEItemInputBus.class);
            registerTileWithModID(MEFluidOutputBus.class);
            registerTileWithModID(MEFluidInputBus.class);
            if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
                registerTileWithModID(MEGasOutputBus.class);
                registerTileWithModID(MEGasInputBus.class);
            }
            registerTileWithModID(MEPatternProvider.class);
            registerTileWithModID(MEPatternMirrorImage.class);
        }
        if (Mods.BM2.isPresent()) {
            GameRegistry.registerTileEntity(TileWillProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilewillproviderinput"));
            GameRegistry.registerTileEntity(TileWillProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilewillprovideroutput"));
            GameRegistry.registerTileEntity(TileLifeEssenceProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilelifeessenceproviderinput"));
            GameRegistry.registerTileEntity(TileLifeEssenceProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilelifeessenceprovideroutput"));
        }
        if (Mods.TC6.isPresent()) {
            GameRegistry.registerTileEntity(TileAspectProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tileaspectproviderinput"));
            GameRegistry.registerTileEntity(TileAspectProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tileaspectprovideroutput"));
        }
        if (Mods.EXU2.isPresent()) {
            GameRegistry.registerTileEntity(TileGridProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilegridproviderinput"));
            GameRegistry.registerTileEntity(TileGridProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilegridprovideroutput"));
            GameRegistry.registerTileEntity(TileRainbowProvider.class, new ResourceLocation(ModularMachinery.MODID, "tilerainbowprovider"));
        }
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            GameRegistry.registerTileEntity(TileStarlightInput.class, new ResourceLocation(ModularMachinery.MODID, "tilestarlightinput"));
            GameRegistry.registerTileEntity(TileStarlightOutput.class, new ResourceLocation(ModularMachinery.MODID, "tilestarlightoutput"));
            GameRegistry.registerTileEntity(TileConstellationProvider.class, new ResourceLocation(ModularMachinery.MODID, "tileconstellationprovider"));
        }
        if (Mods.NATURESAURA.isPresent()) {
            GameRegistry.registerTileEntity(TileAuraProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tileauraproviderinput"));
            GameRegistry.registerTileEntity(TileAuraProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tileauraprovideroutput"));
        }
        if (Mods.BOTANIA.isPresent()) {
            GameRegistry.registerTileEntity(TileManaProvider.Input.class, new ResourceLocation(ModularMachinery.MODID, "tilemanainput"));
            GameRegistry.registerTileEntity(TileManaProvider.Output.class, new ResourceLocation(ModularMachinery.MODID, "tilemanaoutput"));
        }
        if (Mods.TA.isPresent()) {
            GameRegistry.registerTileEntity(TileImpetusComponent.Input.class, new ResourceLocation(ModularMachinery.MODID, "impetusinput"));
            GameRegistry.registerTileEntity(TileImpetusComponent.Output.class, new ResourceLocation(ModularMachinery.MODID, "impetusoutput"));
        }

        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            registerIntegrationTile(TileRadiationProvider.Input.class);
            registerIntegrationTile(TileRadiationProvider.Output.class);
            registerIntegrationTile(TileScrubberProvider.class);
        }

        if (Mods.BM2.isPresent()) {
            registerIntegrationTile(TileWillMultiChunkProvider.Input.class);
            registerIntegrationTile(TileWillMultiChunkProvider.Output.class);
            registerIntegrationTile(TileMeteorProvider.Output.class);
        }

        if (Mods.TC6.isPresent()) {
            registerIntegrationTile(TileFluxProvider.Input.class);
            registerIntegrationTile(TileFluxProvider.Output.class);
            registerIntegrationTile(TileVisProvider.Input.class);
            registerIntegrationTile(TileVisProvider.Output.class);
        }

        if (Mods.ABYSSALCRAFT.isPresent()) {
            registerIntegrationTile(TilePotentialEnergyProvider.Input.class);
            registerIntegrationTile(TilePotentialEnergyProvider.Output.class);
        }

        if (Mods.ICE_AND_FIRE.isPresent()) {
            registerIntegrationTile(TileDragonBreathProvider.class);
        }

        if (Mods.MEKANISM.isPresent()) {
            registerIntegrationTile(TileLaserProvider.class);
            registerIntegrationTile(TileHeatProvider.Input.class);
            registerIntegrationTile(TileHeatProvider.Output.class);
        }

        // Always present
        registerIntegrationTile(TileBiomeProvider.class);
        registerIntegrationTile(TileDimensionProvider.class);

    }

    private static void registerExampleStatedMachineComponent() {
        registerStatedMachineComponent((BlockStatedMachineComponent)
            new BlockStatedMachineComponent().setRegistryName("crushing_wheels"));
    }

    private static void registerCustomStatedMachineComponent() {
        for (final BlockStatedMachineComponent block : BlockStatedMachineComponent.WAIT_FOR_REGISTRY) {
            registerStatedMachineComponent(block);
        }
        BlockStatedMachineComponent.WAIT_FOR_REGISTRY.clear();
    }

    public static void registerStatedMachineComponent(final BlockStatedMachineComponent block) {
        prepareRegisterWithCustomName(block);
        ItemBlockCustomName itemBlock = new ItemBlockCustomName(block) {
            @Nonnull
            @Override
            @SideOnly(Side.CLIENT)
            public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
                return block.getLocalizedName();
            }
        };
        itemBlock.setRegistryName(block.getRegistryName());
        prepareItemBlockRegisterWithCustomName(itemBlock);
    }

    private static void registerCustomControllers() {
        if (Config.onlyOneMachineController) {
            return;
        }

        List<DynamicMachine> waitForLoadMachines = MachineRegistry.getWaitForLoadMachines();
        for (MachineBuilder builder : MachineBuilder.PRE_LOAD_MACHINES.values()) {
            waitForLoadMachines.add(builder.getMachine());
        }
        MachineBuilder.PRE_LOAD_MACHINES.clear();

        if (Config.mocCompatibleMode) {
            for (DynamicMachine machine : waitForLoadMachines) {
                if (machine.isFactoryOnly()) {
                    continue;
                }

                BlockController ctrlBlock = prepareRegisterWithCustomName(new BlockController("modularcontroller", machine));
                BlockController.MOC_MACHINE_CONTROLLERS.put(machine, ctrlBlock);

                ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(ctrlBlock).setRegistryName(Objects.requireNonNull(ctrlBlock.getRegistryName()));
                prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
            }
        }

        for (DynamicMachine machine : waitForLoadMachines) {
            if (machine.isHasFactory() || Config.enableFactoryControllerByDefault) {
                BlockFactoryController factoryBlock = prepareRegisterWithCustomName(new BlockFactoryController(machine));
                BlockFactoryController.FACTORY_CONTROLLERS.put(machine, factoryBlock);

                ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(factoryBlock).setRegistryName(Objects.requireNonNull(factoryBlock.getRegistryName()));
                prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
            }

            if (machine.isFactoryOnly()) {
                continue;
            }

            BlockController ctrlBlock = prepareRegisterWithCustomName(new BlockController(machine));
            BlockController.MACHINE_CONTROLLERS.put(machine, ctrlBlock);

            ItemBlockController ctrlBlockItem = (ItemBlockController) new ItemBlockController(ctrlBlock).setRegistryName(Objects.requireNonNull(ctrlBlock.getRegistryName()));
            prepareItemBlockRegisterWithCustomName(ctrlBlockItem);
        }
    }

    private static void registerBlockModels() {
        for (Block block : blockModelRegister) {
            ModularMachinery.proxy.registerBlockModel(block);
        }
    }

    private static void registerTile(Class<? extends TileEntity> tile, String name) {
        GameRegistry.registerTileEntity(tile, name);
    }

    private static void registerTile(Class<? extends TileEntity> tile) {
        registerTile(tile, tile.getSimpleName().toLowerCase());
    }

    private static void registerTileWithModID(final Class<? extends TileEntity> aClass) {
        GameRegistry.registerTileEntity(aClass, new ResourceLocation(ModularMachinery.MODID, aClass.getSimpleName().toLowerCase()));
    }

    private static ItemBlock prepareItemBlockRegister(Block block) {
        if (block instanceof BlockMachineComponent) {
            if (block instanceof BlockMEMachineComponent) {
                return prepareItemBlockRegister(new ItemBlockMEMachineComponent(block));
            } else if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockMachineComponentCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlockMachineComponent(block));
            }
        } else {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlock(block));
            }
        }
    }

    private static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        String name = item.getBlock().getClass().getSimpleName().toLowerCase();
        item.setRegistryName(ModularMachinery.MODID, name).setTranslationKey(ModularMachinery.MODID + '.' + name);

        RegistryItems.ITEM_BLOCKS.add(item);
        return item;
    }

    private static <T extends ItemBlock> T prepareItemBlockRegisterWithCustomName(T item) {
        RegistryItems.ITEM_BLOCKS_WITH_CUSTOM_NAME.add(item);
        return item;
    }

    private static <T extends Block> T prepareRegister(T block) {
        String name = block.getClass().getSimpleName().toLowerCase();
        block.setRegistryName(ModularMachinery.MODID, name).setTranslationKey(ModularMachinery.MODID + '.' + name);

        return prepareRegisterWithCustomName(block);
    }

    private static <T extends Block> T prepareRegisterWithCustomName(T block) {
        blockModelRegister.add(block);
        CommonProxy.registryPrimer.register(block);
        if (block instanceof BlockDynamicColor) {
            pendingIBlockColorBlocks.add((BlockDynamicColor) block);
        }
        return block;
    }

    public static void writeAllCustomControllerModels() throws IOException {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        for (BlockController controller : BlockController.MACHINE_CONTROLLERS.values()) {
            writeMachineControllerModelInternal(resourceManager, controller);
        }
        for (BlockFactoryController controller : BlockFactoryController.FACTORY_CONTROLLERS.values()) {
            writeFactoryControllerModelInternal(resourceManager, controller);
        }
    }

    private static void writeMachineControllerModelInternal(IResourceManager resourceManager, BlockController controller) throws IOException {
        IResource blockStateResource = resourceManager.getResource(
            new ResourceLocation(ModularMachinery.MODID, "blockstates/block_machine_controller.json"));

        File blockStateFile = new File("resources/modularmachinery/blockstates/" + controller.getRegistryName().getPath() + ".json");
        if (blockStateFile.exists()) {
            return;
        }
        final InputStream inputStream = blockStateResource.getInputStream();
        final FileOutputStream fileOutputStream = FileUtils.openOutputStream(blockStateFile);
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }

    private static void writeFactoryControllerModelInternal(IResourceManager resourceManager, BlockFactoryController controller) throws IOException {
        IResource blockStateResource = resourceManager.getResource(
            new ResourceLocation(ModularMachinery.MODID, "blockstates/block_factory_controller.json"));

        File blockStateFile = new File("resources/modularmachinery/blockstates/" + controller.getRegistryName().getPath() + ".json");
        if (blockStateFile.exists()) {
            return;
        }
        final InputStream inputStream = blockStateResource.getInputStream();
        final FileOutputStream fileOutputStream = FileUtils.openOutputStream(blockStateFile);
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }

    private static <T extends Block> T prepareIntegrationBlock(T block, String path) {
        block.setRegistryName(ModularMachinery.MODID, path);
        block.setTranslationKey(ModularMachinery.MODID + '.' + path);
        return prepareRegisterWithCustomName(block);
    }

    private static void registerIntegrationTile(Class<? extends TileEntity> tile) {
        String path = tile.getCanonicalName().substring(tile.getPackage().getName().length()).replace(".", "").toLowerCase(java.util.Locale.ROOT);
        GameRegistry.registerTileEntity(tile, new ResourceLocation(ModularMachinery.MODID, path));
    }

}
