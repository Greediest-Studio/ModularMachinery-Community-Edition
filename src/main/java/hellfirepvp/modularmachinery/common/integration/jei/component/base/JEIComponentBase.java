// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.integration.jei.component.base;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;

import java.util.List;

public abstract class JEIComponentBase<T> extends ComponentRequirement.JEIComponent<T>{
    private final T requirement;
    private final Class<T> requirementType;

    public JEIComponentBase(T requirement, Class<T> requirementType) {
        this.requirement = requirement;
        this.requirementType = requirementType;
    }

    @Override
    public Class<T> getJEIRequirementClass() {
        return requirementType;
    }

    @Override
    public List<T> getJEIIORequirements() {
        return Lists.newArrayList(requirement);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip) {
        // Not used
    }
}
