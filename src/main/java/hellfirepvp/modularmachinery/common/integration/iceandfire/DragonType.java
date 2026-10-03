// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.iceandfire;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.IStringSerializable;

@MethodsReturnNonnullByDefault
public enum DragonType implements IStringSerializable {
    EMPTY,
    ICE,
    FIRE,
    LIGHTNING,;

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }
}
