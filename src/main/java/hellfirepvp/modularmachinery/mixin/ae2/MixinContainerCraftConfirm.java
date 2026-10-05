// 移植自 MMCE-AdvancedBuilderTool，按 GPL-3.0 许可融合。
package hellfirepvp.modularmachinery.mixin.ae2;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.container.implementations.ContainerCraftConfirm;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.CraftingConfirmBridge;
import hellfirepvp.modularmachinery.common.integration.ae2.builder.CraftingRequester;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContainerCraftConfirm.class)
public abstract class MixinContainerCraftConfirm implements CraftingConfirmBridge {

    @Unique
    private CraftingRequester mmce$builderRequester;

    @Override
    public void mmce$setRequester(CraftingRequester requester) {
        this.mmce$builderRequester = requester;
    }

    @Redirect(
            method = "startJob",
            at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingGrid;submitJob(Lappeng/api/networking/crafting/ICraftingJob;Lappeng/api/networking/crafting/ICraftingRequester;Lappeng/api/networking/crafting/ICraftingCPU;ZLappeng/api/networking/security/IActionSource;)Lappeng/api/networking/crafting/ICraftingLink;"),
            remap = false
    )
    private ICraftingLink mmce$submitBuilderJob(ICraftingGrid craftingGrid, ICraftingJob job, ICraftingRequester originalRequester, ICraftingCPU cpu, boolean prioritizePower, IActionSource source) {
        if (mmce$builderRequester != null && !mmce$builderRequester.mmce$canSubmitCraft()) return null;
        ICraftingLink link = craftingGrid.submitJob(job, mmce$builderRequester == null ? originalRequester : mmce$builderRequester, cpu, prioritizePower, source);
        if (mmce$builderRequester != null) {
            mmce$builderRequester.mmce$setCraftingLink(link);
        }
        return link;
    }
}
