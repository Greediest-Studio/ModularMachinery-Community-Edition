// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import hellfirepvp.modularmachinery.common.tiles.TileLaserProvider;
import mcjty.theoneprobe.api.IProbeInfo;

public class LaserInfoProvider extends BaseInfoProvider<TileLaserProvider> {

    public LaserInfoProvider() {
        super(TileLaserProvider.class);
    }

    @Override
    protected String getName() {
        return "laser_provider";
    }

    @Override
    protected void addProbeInfo(IProbeInfo iProbeInfo, TileLaserProvider hatch) {
        iProbeInfo.text(wrapInLoc("top.modularmachinery.laser") + " " + hatch.getStoredEnergy());
    }
}
