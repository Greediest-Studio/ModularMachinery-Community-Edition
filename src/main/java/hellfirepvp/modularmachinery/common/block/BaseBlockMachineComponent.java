// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;

import javax.annotation.Nonnull;

/**
 * The only purpose of this class is to provide some base initialisations for all blocks implemented by this mod. Instead of copying the same hardcoded values, I centralised everything here to
 * improve consistency and maintainability.
 */
public class BaseBlockMachineComponent extends BlockMachineComponent {

    private static final Material BASE_MATERIAL = Material.IRON;
    private static final float BASE_HARDNESS = 2F;
    private static final float BASE_RESISTANCE = 10F;
    private static final SoundType BASE_SOUND_TYPE = SoundType.METAL;
    private static final String BASE_TOOL_CLASS = "pickaxe";
    private static final int BASE_TOOL_LEVEL = 1;

    public BaseBlockMachineComponent() {
        super(BASE_MATERIAL);
        setHardness(BASE_HARDNESS);
        setResistance(BASE_RESISTANCE);
        setSoundType(BASE_SOUND_TYPE);
        setHarvestLevel(BASE_TOOL_CLASS, BASE_TOOL_LEVEL);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    @Nonnull
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState ignored) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @Nonnull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
