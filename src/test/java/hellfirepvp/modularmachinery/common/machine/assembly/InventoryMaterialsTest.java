package hellfirepvp.modularmachinery.common.machine.assembly;

import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.junit.jupiter.api.*;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class InventoryMaterialsTest {
    @BeforeAll static void bootstrap() throws Exception {
        Bootstrap.register();
        // JUnit 没有 Forge 的注解扫描，显式注入流体能力供真实 ItemStack 使用。
        if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null) {
            CapabilityFluidHandler.register();
            Field providers = CapabilityManager.class.getDeclaredField("providers"); providers.setAccessible(true);
            Map<String, Capability<?>> caps = (Map<String, Capability<?>>) providers.get(CapabilityManager.INSTANCE);
            CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY = (Capability<IFluidHandlerItem>) caps.get(IFluidHandlerItem.class.getName().intern());
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY = (Capability<IFluidHandler>) caps.get(IFluidHandler.class.getName().intern());
        }
    }

    @Test void itemShortfallDoesNotConsumeAndSplitStacksCanPay() throws Exception {
        EntityPlayer player = player();
        player.inventory.mainInventory.set(0, new ItemStack(Items.STICK, 1));
        assertNull(InventoryMaterials.takeItem(player, new ItemStack(Items.STICK, 2)));
        assertEquals(1, player.inventory.mainInventory.get(0).getCount());
        player.inventory.mainInventory.set(1, new ItemStack(Items.STICK, 1));
        MaterialPayment paid = InventoryMaterials.takeItem(player, new ItemStack(Items.STICK, 2));
        assertNotNull(paid);
        assertEquals(0, count(player, Items.STICK));
        paid.refund(); paid.refund();
        assertEquals(2, count(player, Items.STICK));
    }

    @Test void changedFluidContainerIsWrittenBackAndRefundRestoresOriginal() throws Exception {
        EntityPlayer player = player();
        ItemStack full = new ItemStack(new TestTankItem());
        FluidUtil.getFluidHandler(full).fill(new FluidStack(FluidRegistry.WATER, 1000), true);
        player.inventory.mainInventory.set(0, full);
        MaterialPayment payment = InventoryMaterials.takeFluid(player, new FluidStack(FluidRegistry.WATER, 1000));
        assertNotNull(payment);
        assertSame(Items.GLASS_BOTTLE, player.inventory.mainInventory.get(0).getItem());
        payment.refund();
        assertEquals(1000, FluidUtil.getFluidContained(player.inventory.mainInventory.get(0)).amount);
    }

    @Test void fluidShortfallDoesNotMutateContainer() throws Exception {
        EntityPlayer player = player();
        ItemStack full = new ItemStack(new TestTankItem());
        FluidUtil.getFluidHandler(full).fill(new FluidStack(FluidRegistry.WATER, 400), true);
        player.inventory.mainInventory.set(0, full);
        assertNull(InventoryMaterials.takeFluid(player, new FluidStack(FluidRegistry.WATER, 1000)));
        assertEquals(400, FluidUtil.getFluidContained(full).amount);
    }

    @Test void fluidReturnCommitsOnlyWhenEntireAmountFits() throws Exception {
        EntityPlayer player = player();
        ItemStack tank = new ItemStack(new TestTankItem());
        FluidUtil.getFluidHandler(tank).fill(new FluidStack(FluidRegistry.WATER, 500), true);
        player.inventory.mainInventory.set(0, tank);
        assertNull(InventoryMaterials.prepareFluidReturn(player, new FluidStack(FluidRegistry.WATER, 1000)));
        assertEquals(500, FluidUtil.getFluidContained(tank).amount);
        Runnable fill = InventoryMaterials.prepareFluidReturn(player, new FluidStack(FluidRegistry.WATER, 300));
        assertNotNull(fill);
        assertEquals(500, FluidUtil.getFluidContained(tank).amount);
        fill.run();
        assertEquals(800, FluidUtil.getFluidContained(player.inventory.mainInventory.get(0)).amount);
    }

    private static int count(EntityPlayer player, Item item) {
        return player.inventory.mainInventory.stream().filter(stack -> stack.getItem() == item).mapToInt(ItemStack::getCount).sum();
    }
    private static EntityPlayer player() throws Exception {
        // 材料结算不访问世界；绕过玩家构造中的服务器启动，只提供真实背包。
        Field field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
        EntityPlayer player = (EntityPlayer) ((Unsafe) field.get(null)).allocateInstance(EntityPlayerMP.class);
        player.inventory = new InventoryPlayer(player);
        player.capabilities = new PlayerCapabilities();
        return player;
    }
    private static final class TestTankItem extends Item {
        TestTankItem() { setMaxStackSize(1); }
        @Override public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
            return new FluidHandlerItemStack(stack, 1000) {
                @Override public ItemStack getContainer() { return getFluid() == null ? new ItemStack(Items.GLASS_BOTTLE) : super.getContainer(); }
            };
        }
    }
}
