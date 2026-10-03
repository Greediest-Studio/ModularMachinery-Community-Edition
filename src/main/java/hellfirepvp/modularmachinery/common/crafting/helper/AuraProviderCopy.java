package hellfirepvp.modularmachinery.common.crafting.helper;

import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Aura;
import hellfirepvp.modularmachinery.common.tiles.TileAuraProvider;

/** 同一区块的模拟需求共享余额，单位与 Nature's Aura API 一致。 */
public class AuraProviderCopy {
    private final TileAuraProvider original;
    private final IAuraType type;
    private long amount;

    public AuraProviderCopy(TileAuraProvider original) {
        this.original = original;
        Aura aura = original.getAura();
        this.type = aura.getType();
        this.amount = aura.getAmount();
    }

    public TileAuraProvider getOriginal() {
        return original;
    }

    public long available(IAuraType requiredType, IOType io, int bound) {
        if (type != requiredType) {
            return 0;
        }
        return Math.max(0L, io == IOType.INPUT ? amount - bound : (long) bound - amount);
    }

    public int transfer(IAuraType requiredType, IOType io, int bound, int requested, boolean simulate) {
        int moved = (int) Math.min(requested, available(requiredType, io, bound));
        if (!simulate && moved > 0) {
            moved = original.transferAura(new Aura(moved, type), io);
        }
        amount += io == IOType.INPUT ? -(long) moved : moved;
        return moved;
    }
}
