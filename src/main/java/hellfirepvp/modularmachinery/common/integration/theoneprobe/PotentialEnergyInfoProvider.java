// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import hellfirepvp.modularmachinery.common.tiles.TilePotentialEnergyProvider;
import mcjty.theoneprobe.api.IProbeInfo;

public class PotentialEnergyInfoProvider extends BaseInfoProvider<TilePotentialEnergyProvider> {

    public PotentialEnergyInfoProvider() {
        super(TilePotentialEnergyProvider.class);
    }

    @Override
    protected String getName() {
        return "potential_energy";
    }

    @Override
    protected void addProbeInfo(IProbeInfo iProbeInfo, TilePotentialEnergyProvider hatch) {
        iProbeInfo.text(wrapInLoc("top.modularmachinery.potential_energy") + " " + hatch.getContainedEnergy());
    }
}
