// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei;

/**
 * I know the Object.equals exists, but I wanted a somewhat clean way to enforce that some classes have a different implementation that actually ensures whether two objects are equal or not.
 */
public interface IRequiresEquals<T> {
    boolean equalsTo(T other);
}
