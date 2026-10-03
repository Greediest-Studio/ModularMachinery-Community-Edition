// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import hellfirepvp.modularmachinery.common.tiles.TileHeatProvider;
import mcjty.theoneprobe.api.IProbeInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;

public class HeatInfoProvider extends BaseInfoProvider<TileHeatProvider> {

    public HeatInfoProvider() {
        super(TileHeatProvider.class);
    }

    @Override
    protected String getName() {
        return "heat_provider";
    }

    @Override
    protected void addProbeInfo(IProbeInfo iProbeInfo, TileHeatProvider hatch) {
        iProbeInfo.text(wrapInLoc("top.modularmachinery.heat") + " " + MekanismUtils.getTemperatureDisplay(hatch.getTemperature(), UnitDisplayUtils.TemperatureUnit.CELSIUS));
    }
}
