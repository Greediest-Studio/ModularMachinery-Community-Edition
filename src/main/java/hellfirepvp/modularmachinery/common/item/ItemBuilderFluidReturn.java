package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import javax.annotation.Nullable;

/** 仅在流体退款无处存放时产生，保存精确余量，不能配方获取。 */
public final class ItemBuilderFluidReturn extends Item {
    public ItemBuilderFluidReturn() {
        setRegistryName("modularmachinery", "builder_fluid_return");
        setTranslationKey("modularmachinery.builder_fluid_return");
        setMaxStackSize(1);
    }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStack(stack, Integer.MAX_VALUE);
    }
    public static ItemStack create(FluidStack fluid) {
        ItemStack stack = new ItemStack(ItemsMM.builderFluidReturn);
        FluidUtil.getFluidHandler(stack).fill(fluid.copy(), true);
        return stack;
    }
    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable net.minecraft.world.World world, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid != null) tooltip.add(fluid.amount + " mB " + fluid.getLocalizedName());
    }
}
