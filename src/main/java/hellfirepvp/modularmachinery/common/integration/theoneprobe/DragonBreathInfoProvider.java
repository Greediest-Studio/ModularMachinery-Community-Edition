// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import hellfirepvp.modularmachinery.common.tiles.TileDragonBreathProvider;
import mcjty.theoneprobe.api.IProbeInfo;

public class DragonBreathInfoProvider extends BaseInfoProvider<TileDragonBreathProvider> {

    public DragonBreathInfoProvider() {
        super(TileDragonBreathProvider.class);
    }

    @Override
    protected String getName() {
        return "dragon_breath";
    }

    @Override
    protected void addProbeInfo(IProbeInfo iProbeInfo, TileDragonBreathProvider hatch) {
        iProbeInfo.text(wrapInLoc((hatch.isTypeLocked() ? "top.modularmachinery.dragon_breath.locked.true" : "top.modularmachinery.dragon_breath.locked.false")));
        String dragonTypeKey = hatch.getType() == null ? "N/A" : String.format("dragon.type.%s", hatch.getType().name().toLowerCase());
        iProbeInfo.text(wrapInLoc("top.modularmachinery.dragon_breath.type")  + " " + wrapInLoc(dragonTypeKey));
        iProbeInfo.text(wrapInLoc("top.modularmachinery.dragon_breath.quantity")  + " " + hatch.getCharges());
    }
}
