// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.nuclearcraft;

import hellfirepvp.modularmachinery.common.machine.IOType;

public interface IRequirementRadiation {
    double getAmount();
    IOType getType();
    int getChunkRange();
}
