// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper;

// Indicates an unexpected invariant violation
public class ConsistencyException extends RuntimeException {
    public ConsistencyException(String message) {
        super(message);
    }
}
