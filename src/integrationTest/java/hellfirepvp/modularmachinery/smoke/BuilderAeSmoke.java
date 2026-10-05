package hellfirepvp.modularmachinery.smoke;

import appeng.container.implementations.ContainerCraftConfirm;
import appeng.api.storage.data.IAEItemStack;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.Ae2GridAccess;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.CraftingConfirmBridge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

/** 单独加载，使无 AE 的验证也能启动。 */
final class BuilderAeSmoke {
    static void verify() {
        if (!CraftingConfirmBridge.class.isAssignableFrom(ContainerCraftConfirm.class)) throw new AssertionError("AE 合成确认 Mixin 未生效");
        IAEItemStack request = Ae2GridAccess.toAeFluidRequest(new FluidStack(FluidRegistry.WATER, 1000), 1000);
        if (Loader.isModLoaded("ae2fc")) {
            if (request == null || request.getStackSize() != 1000) throw new AssertionError("AE 流体合成转换量错误");
        } else if (request != null) throw new AssertionError("未安装流体合成时仍创建了流体请求");
    }
}
