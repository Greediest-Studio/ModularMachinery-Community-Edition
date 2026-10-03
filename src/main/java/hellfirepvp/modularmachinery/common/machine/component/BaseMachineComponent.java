// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.machine.component;

import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

public abstract class BaseMachineComponent<T> extends MachineComponent<IRequirementHandler<T>> {

    private final IRequirementHandler<T> handler;

    public BaseMachineComponent(IOType ioType, IRequirementHandler<T> handler) {
        super(ioType);
        this.handler = handler;
    }

    @Override
    public IRequirementHandler<T> getContainerProvider() {
        return handler;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }
}
