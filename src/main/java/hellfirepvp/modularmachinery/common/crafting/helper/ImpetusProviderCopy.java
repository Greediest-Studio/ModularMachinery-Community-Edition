package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.tiles.TileImpetusComponent;

public class ImpetusProviderCopy {
    private final TileImpetusComponent original;

    private int impetus;

    public ImpetusProviderCopy(final TileImpetusComponent original) {
        this.original = original;
        this.impetus = original.getImpetus();
    }

    public int consumeImpetus(int amount) {
        int maxConsume = Math.min(impetus, amount);
        impetus -= maxConsume;
        return maxConsume;
    }

    public int supplyImpetus(int amount) {
        int maxSupply = Math.min(TileImpetusComponent.CAPACITY - impetus, amount);
        this.impetus += maxSupply;
        return maxSupply;
    }

    public TileImpetusComponent getOriginal() {
        return original;
    }

    public int getImpetus() {
        return impetus;
    }
}
