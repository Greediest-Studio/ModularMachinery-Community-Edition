// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.crafting.helper.RequirementPrerequisiteFailedException;

public class RequirementValidator {

    private static RequirementValidator instance;

    public static RequirementValidator getInstance() {
        if (instance == null) {instance = new RequirementValidator();}
        return instance;
    }

    private RequirementValidator() {}

    public void validateNotNegative(double value, String errorMessage) {
        if (value < 0) {throw new RequirementPrerequisiteFailedException(errorMessage);}
    }

    public void validateNotNull(Object value, String errorMessage) {
        if (value == null) {throw new RequirementPrerequisiteFailedException(errorMessage);}
    }

    public void validateNotAbove(double value, double max, String errorMessage) {
        if (value > max) {throw new RequirementPrerequisiteFailedException(errorMessage);}
    }
}
