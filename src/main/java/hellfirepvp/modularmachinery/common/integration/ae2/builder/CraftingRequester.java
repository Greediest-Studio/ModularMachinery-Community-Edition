// 移植自 MMCE-AdvancedBuilderTool，按 GPL-3.0 许可融合。
package hellfirepvp.modularmachinery.common.integration.ae2.builder;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;

public interface CraftingRequester extends ICraftingRequester {

    void mmce$setCraftingLink(ICraftingLink link);

    boolean mmce$canSubmitCraft();
}
