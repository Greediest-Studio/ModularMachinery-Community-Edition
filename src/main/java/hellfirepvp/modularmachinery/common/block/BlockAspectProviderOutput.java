package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.tiles.TileAspectProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

public class BlockAspectProviderOutput extends BlockAspectProvider {

    public BlockAspectProviderOutput() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Nullable
    @Override
    @Optional.Method(modid = "thaumcraft")
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAspectProvider.Output();
    }

}
