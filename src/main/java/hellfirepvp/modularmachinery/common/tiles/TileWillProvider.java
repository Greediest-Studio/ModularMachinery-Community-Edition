package hellfirepvp.modularmachinery.common.tiles;

import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentWillProvider;

import javax.annotation.Nullable;

public abstract class TileWillProvider extends TileColorableMachineComponent implements MachineComponentTile {

    public double getWill(EnumDemonWillType willType) {
        return WorldDemonWillHandler.getCurrentWill(this.world, this.pos, willType);
    }

    public void addWill(double willValue, EnumDemonWillType willType) {
        WorldDemonWillHandler.fillWill(this.world, this.pos, willType, willValue, true);
    }

    public void removeWill(double willValue, EnumDemonWillType willType) {
        WorldDemonWillHandler.drainWill(this.world, this.pos, willType, willValue, true);
    }

    // 由主线程调用：检查、扣款和不足额退款必须在同一次操作内完成。
    public boolean tryRemoveWill(double willValue, EnumDemonWillType willType, double minimum) {
        if (willValue < 0 || getWill(willType) - willValue < Math.max(0, minimum)) {
            return false;
        }
        double removed = WorldDemonWillHandler.drainWill(world, pos, willType, willValue, true);
        if (removed >= willValue) {
            return true;
        }
        if (removed > 0) {
            WorldDemonWillHandler.fillWill(world, pos, willType, removed, true);
        }
        return false;
    }

    public static class Input extends TileWillProvider {

        @Nullable
        @Override
        public MachineComponentWillProvider provideComponent() {
            return new MachineComponentWillProvider(this, IOType.INPUT);
        }
    }

    public static class Output extends TileWillProvider {

        @Nullable
        @Override
        public MachineComponentWillProvider provideComponent() {
            return new MachineComponentWillProvider(this, IOType.OUTPUT);
        }
    }
}
