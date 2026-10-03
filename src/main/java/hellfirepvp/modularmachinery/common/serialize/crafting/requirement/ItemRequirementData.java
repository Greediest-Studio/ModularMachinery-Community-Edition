package hellfirepvp.modularmachinery.common.serialize.crafting.requirement;

import hellfirepvp.modularmachinery.common.serialize.DataStructure;
import hellfirepvp.modularmachinery.common.serialize.DataValue;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.Objects;

public final class ItemRequirementData extends DataStructure {
    private final DataValue<Either> item = string("item").map(text -> {
        String[] split = text.split(":", -1);
        if (split.length == 1) { return new Either(null, split[0], null); }
        if (split.length != 2) { throw new IllegalStateException("Invalid item string: " + text); }
        if ("ore".equals(split[0])) { return new Either(null, split[1], null); }
        // 保留原实现先查找完整 ID、再解析 @meta 的顺序。
        ResourceLocation location = new ResourceLocation(split[0], split[1]);
        net.minecraft.item.Item value = ForgeRegistries.ITEMS.getValue(location);
        if (value == null) { throw new IllegalStateException("Item " + location + " not found"); }
        String[] metadata = split[1].split("@", -1);
        Integer meta = null;
        if (metadata.length > 1) {
            try { meta = Integer.valueOf(metadata[1]); } catch (NumberFormatException ignored) { }
        }
        return new Either(meta == null ? new ItemStack(value) : new ItemStack(value, 1, meta), null, null);
    });
    private final DataValue<Integer> amount = integer("amount").def(() -> 1);
    private final DataValue<Float> chance = floatValue("chance").def(() -> 1F);
    private final DataValue<Integer> fuelTime = integer("time").def(() -> -1);
    private final DataValue<NBTTagCompound> nbt = string("nbt").map(ItemRequirementData::parseNbt);
    private final DataValue<NBTTagCompound> nbtDisplay = string("nbt-display").map(ItemRequirementData::parseNbt);

    public Either getItem() { return item.getValue(); }
    public int getAmount() { return amount.getValue(); }
    public float getChance() { return chance.getValue(); }
    public int getFuelTime() { return fuelTime.getValue(); }
    public NBTTagCompound getNbt() { return nbt.getValue(); }
    public NBTTagCompound getNbtDisplay() { return nbtDisplay.getValue(); }

    private static NBTTagCompound parseNbt(String text) {
        if (text.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c))) { return null; }
        try {
            return NBTJsonDeserializer.deserialize(text);
        } catch (NBTException error) {
            // Kotlin 不要求声明受检异常；Java lambda 保留同一异常实例及类型。
            return ItemRequirementData.<RuntimeException, NBTTagCompound>rethrow(error);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T rethrow(Throwable error) throws E { throw (E) error; }

    public static final class Either {
        private final ItemStack stack;
        private final String oreDict;
        private final Integer fuelTime;

        public Either() { this(null, null, null); }
        public Either(ItemStack stack, String oreDict, Integer fuelTime) {
            this.stack = stack;
            this.oreDict = oreDict;
            this.fuelTime = fuelTime;
        }
        public ItemStack getStack() { return stack; }
        public String getOreDict() { return oreDict; }
        public Integer getFuelTime() { return fuelTime; }
        public ItemStack component1() { return stack; }
        public String component2() { return oreDict; }
        public Integer component3() { return fuelTime; }
        public Either copy(ItemStack stack, String oreDict, Integer fuelTime) { return new Either(stack, oreDict, fuelTime); }
        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof Either)) { return false; }
            Either that = (Either) other;
            return Objects.equals(stack, that.stack) && Objects.equals(oreDict, that.oreDict)
                    && Objects.equals(fuelTime, that.fuelTime);
        }
        @Override public int hashCode() {
            return (Objects.hashCode(stack) * 31 + Objects.hashCode(oreDict)) * 31 + Objects.hashCode(fuelTime);
        }
        @Override public String toString() {
            return "Either(stack=" + stack + ", oreDict=" + oreDict + ", fuelTime=" + fuelTime + ")";
        }
    }
}
