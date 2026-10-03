// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper.snapshot;

import hellfirepvp.modularmachinery.common.crafting.helper.IMultiChunkRequirement;

public interface ISnapshot<T extends IMultiChunkRequirement> {
    ISnapshot<T> getSnapshotForRange(int range);
    boolean canHandleInput(T requirement);
    boolean canHandleOutput(T requirement);
}
