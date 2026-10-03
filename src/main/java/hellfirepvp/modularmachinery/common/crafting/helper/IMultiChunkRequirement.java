// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.machine.IOType;

// I want multiple inheritance
public interface IMultiChunkRequirement {

    IOType getIOType();

    int getChunkRange();

    double getAmount();

    double getMinPerChunk();

    double getMaxPerChunk();
}
