// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper;

public interface IRequirementHandler<T> {
    CraftCheck canHandle(T requirement);
    void handle(T requirement);

    /** 调用方在服务端主线程内完成复查与提交，失败不消耗资源。 */
    default boolean tryHandle(T requirement) {
        CraftCheck check = canHandle(requirement);
        if (check == null || !check.isSuccess()) return false;
        handle(requirement);
        return true;
    }
}
