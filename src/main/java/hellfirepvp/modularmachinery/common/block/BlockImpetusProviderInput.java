package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.tiles.TileImpetusComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockImpetusProviderInput extends BlockImpetusProvider {

    public static final BlockImpetusProviderInput INSTANCE = new BlockImpetusProviderInput();

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileImpetusComponent.Input();
    }

    @Override
    protected int getIOPresentationColor() {
        return 0x085ca2;
    }
}