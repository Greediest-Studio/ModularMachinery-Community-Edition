package github.kasuminova.mmce.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/** 世界级单调编号；不依赖已加载区块，也不在提供器卸载时回收。 */
public final class PatternProviderGroupData extends WorldSavedData {
    private static final String NAME = "mmce_pattern_provider_groups";
    private long lastGroupId = Integer.MAX_VALUE;

    public PatternProviderGroupData(String name) {
        super(name);
    }

    public static synchronized long allocate(World world, int count) {
        return get(world).allocate(count);
    }

    long allocate(int count) {
        long first = lastGroupId + 1;
        lastGroupId += count;
        markDirty();
        return first;
    }

    public static synchronized void reserve(World world, long lastGroupId) {
        get(world).reserve(lastGroupId);
    }

    void reserve(long lastGroupId) {
        if (lastGroupId > this.lastGroupId) {
            this.lastGroupId = lastGroupId;
            markDirty();
        }
    }

    private static PatternProviderGroupData get(World world) {
        MapStorage storage = world.getMapStorage();
        PatternProviderGroupData data = (PatternProviderGroupData) storage.getOrLoadData(PatternProviderGroupData.class, NAME);
        if (data == null) {
            data = new PatternProviderGroupData(NAME);
            storage.setData(NAME, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastGroupId = Math.max(Integer.MAX_VALUE, compound.getLong("lastGroupId"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("lastGroupId", lastGroupId);
        return compound;
    }
}
