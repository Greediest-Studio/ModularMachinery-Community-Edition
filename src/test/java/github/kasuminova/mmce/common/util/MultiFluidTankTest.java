package github.kasuminova.mmce.common.util;

import net.minecraft.init.Bootstrap;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiFluidTankTest {
    @BeforeClass
    public static void initializeMinecraft() {
        Bootstrap.register();
    }

    @Test
    public void 快照只读取一次槽位且模拟消耗不影响原库存() {
        CountingTank tank = new CountingTank();
        MultiFluidTank snapshot = new MultiFluidTank(tank);
        assertEquals(1, tank.propertyReads);
        assertEquals(100, snapshot.drain(new FluidStack(FluidRegistry.WATER, 100), true).amount);
        assertEquals(1000, tank.getFluidAmount());
        MultiFluidTank independent = new MultiFluidTank(snapshot);
        snapshot.drain(new FluidStack(FluidRegistry.WATER, 100), true);
        assertEquals(900, independent.getTankProperties()[0].getContents().amount);
    }

    @Test
    public void 复制保留单流体单槽语义() {
        MultiFluidTank snapshot = new MultiFluidTank(new CountingTank());
        assertTrue(snapshot.isOneFluidOneSlot());
        assertTrue(new MultiFluidTank(snapshot).isOneFluidOneSlot());
    }

    @Test
    public void 无限输入快照保留自身与共用流体顺序() {
        InfItemFluidHandler handler = new InfItemFluidHandler(new CountingTank());
        handler.fill(new FluidStack(FluidRegistry.LAVA, 200), true);
        MultiFluidTank snapshot = new MultiFluidTank(handler);
        assertEquals(2, snapshot.getTankProperties().length);
        assertEquals(FluidRegistry.LAVA, snapshot.getTankProperties()[0].getContents().getFluid());
        assertEquals(FluidRegistry.WATER, snapshot.getTankProperties()[1].getContents().getFluid());
        snapshot.drain(new FluidStack(FluidRegistry.LAVA, 200), true);
        assertEquals(200, handler.getFluidStackList().get(0).amount);
    }

    private static class CountingTank extends FluidTank implements IOneToOneFluidHandler {
        int propertyReads;
        CountingTank() { super(new FluidStack(FluidRegistry.WATER, 1000), 1000); }
        @Override
        public net.minecraftforge.fluids.capability.IFluidTankProperties[] getTankProperties() {
            propertyReads++;
            return super.getTankProperties();
        }
        @Override
        public boolean isOneFluidOneSlot() { return true; }
    }
}
