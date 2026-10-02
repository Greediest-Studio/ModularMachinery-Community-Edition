package kport.modularmagic.common.tile;

import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentAuraProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;

public abstract class TileAuraProvider extends TileColorableMachineComponent implements MachineComponentTile {

    public void addAura(Aura aura) {
        ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> transferAura(aura, IOType.OUTPUT));
    }

    public void removeAura(Aura aura) {
        ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> transferAura(aura, IOType.INPUT));
    }

    /** 主线程调用；读取、配方数量、上下界和实际转移均使用 API 原生单位。 */
    public int transferAura(Aura aura, IOType io) {
        IAuraChunk auraChunk = IAuraChunk.getAuraChunk(world, pos);
        if (aura.getAmount() <= 0 || aura.getType() != auraChunk.getType()) {
            return 0;
        }
        return io == IOType.INPUT
            ? auraChunk.drainAura(pos, aura.getAmount(), false, false)
            : auraChunk.storeAura(pos, aura.getAmount(), false, false);
    }

    public Aura getAura() {
        IAuraType type = IAuraChunk.getAuraChunk(world, pos).getType();
        int amount = IAuraChunk.getAuraInArea(world, pos, 1);
        return new Aura(amount, type);
    }

    public ChunkPos getChunkPos() {
        BlockPos pos = getPos();
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static class Input extends TileAuraProvider {

        @Nullable
        @Override
        public MachineComponentAuraProvider provideComponent() {
            return new MachineComponentAuraProvider(this, IOType.INPUT);
        }
    }

    public static class Output extends TileAuraProvider {

        @Nullable
        @Override
        public MachineComponentAuraProvider provideComponent() {
            return new MachineComponentAuraProvider(this, IOType.OUTPUT);
        }
    }
}
