// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.crafting.component.BaseComponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to yeet some boilerplate code from the component definitions. See {@link BaseComponent}
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresMod {
    String value();
}
