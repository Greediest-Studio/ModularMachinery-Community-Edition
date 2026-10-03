// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.helper;

// Indicates that a prerequisite was not met while building a requirement
public class RequirementPrerequisiteFailedException extends RuntimeException {
    public RequirementPrerequisiteFailedException(String message) {
        super(message);
    }
}
