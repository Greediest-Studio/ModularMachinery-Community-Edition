package hellfirepvp.modularmachinery.common.machine;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * 机械在 JEI / HEI 中的子分类/标签页元数据定义。
 */
@ZenRegister
@ZenClass("mods.modularmachinery.MachineJEICategory")
public class MachineJEICategory {
    private final String key;
    private String displayName;
    private ItemStack icon = ItemStack.EMPTY;
    private int priority = 0;

    public MachineJEICategory(String key, @Nullable String displayName, ItemStack icon, int priority) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon != null ? icon : ItemStack.EMPTY;
        this.priority = priority;
    }

    public MachineJEICategory(String key, @Nullable String displayName, ItemStack icon) {
        this(key, displayName, icon, 0);
    }

    public MachineJEICategory(String key) {
        this(key, null, ItemStack.EMPTY, 0);
    }

    @ZenMethod
    public static MachineJEICategory create(String key) {
        return new MachineJEICategory(key);
    }

    @ZenMethod
    public static MachineJEICategory create(String key, String displayName) {
        return new MachineJEICategory(key, displayName, ItemStack.EMPTY);
    }

    @ZenMethod
    public static MachineJEICategory create(String key, String displayName, IItemStack icon) {
        return new MachineJEICategory(key, displayName, icon != null ? CraftTweakerMC.getItemStack(icon) : ItemStack.EMPTY);
    }

    @ZenMethod
    public static MachineJEICategory create(String key, String displayName, IItemStack icon, int priority) {
        return new MachineJEICategory(key, displayName, icon != null ? CraftTweakerMC.getItemStack(icon) : ItemStack.EMPTY, priority);
    }

    @ZenGetter("key")
    public String getKey() {
        return key;
    }

    @ZenGetter("displayName")
    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @ZenMethod
    public MachineJEICategory setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ItemStack getIcon() {
        return icon;
    }

    @ZenMethod
    public MachineJEICategory setIcon(IItemStack icon) {
        this.icon = icon != null ? CraftTweakerMC.getItemStack(icon) : ItemStack.EMPTY;
        return this;
    }

    @ZenGetter("priority")
    public int getPriority() {
        return priority;
    }

    @ZenMethod
    public MachineJEICategory setPriority(int priority) {
        this.priority = priority;
        return this;
    }
}

