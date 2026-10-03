// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.component;

import hellfirepvp.modularmachinery.common.util.RequiresMod;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;

import javax.annotation.Nullable;

public abstract class BaseComponent extends ComponentType {

    @Nullable
    @Override
    public String requiresModid() {
        RequiresMod annotation = this.getClass().getAnnotation(RequiresMod.class);
        return (annotation != null) ? annotation.value() : null;
    }
}
