// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import hellfirepvp.modularmachinery.common.tiles.base.AbstractSnapshotMachineComponent;
import mcjty.theoneprobe.api.IProbeInfo;

@SuppressWarnings("raw")
public class SnapshotMachineComponentInfoProvider extends BaseInfoProvider<AbstractSnapshotMachineComponent> {

    public SnapshotMachineComponentInfoProvider() {
        super(AbstractSnapshotMachineComponent.class);
    }

    @Override
    protected String getName() {
        return "snapshot_machine_info_provider";
    }

    @Override
    protected void addProbeInfo(IProbeInfo iProbeInfo, AbstractSnapshotMachineComponent hatch) {
        iProbeInfo.text(wrapInLoc("top.modularmachinery.next_refresh") + " " + hatch.getSecondsUntilNextRefresh());
    }

}
