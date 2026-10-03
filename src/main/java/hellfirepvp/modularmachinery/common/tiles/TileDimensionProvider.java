// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementDimension;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentDimensionProvider;

import hellfirepvp.modularmachinery.common.tiles.base.AbstractSnapshotMachineComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.IRequirementHandler;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;

import javax.annotation.Nullable;

public class TileDimensionProvider extends AbstractSnapshotMachineComponent<RequirementDimension> implements MachineComponentTile, IRequirementHandler<RequirementDimension> {

    private int dimensionId;

    @Override
    protected void updateSnapshot() {
        lock.writeLock().lock();
        try {
            this.dimensionId = this.world.provider.getDimension();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected CraftCheck checkSnapshot(RequirementDimension requirement) {
        return this.dimensionId == requirement.getDimension().getId() ? CraftCheck.success() : CraftCheck.failure("error.modularmachinery.requirement.missing.dimension");
    }

    @Override
    public void handle(RequirementDimension requirement) {
        // *eats the dimension*
        super.handle(requirement);
    }

    @Nullable
    @Override
    public MachineComponentDimensionProvider provideComponent() {
        return new MachineComponentDimensionProvider(IOType.INPUT, this);
    }
}
