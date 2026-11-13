package github.kasuminova.mmce.common.upgrade;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.UpgradeEventHandlerCT;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineUpgrade")
public abstract class MachineUpgrade {

    protected final UpgradeType                                type;
    protected final Map<Class<?>, List<UpgradeEventHandlerCT>> eventProcessor  = new HashMap<>();
    protected final List<StaticUpgradeModifier>                staticModifiers = new ArrayList<>();

    protected TileUpgradeBus parentBus = null;

    protected int            stackSize          = 1;
    protected NBTTagCompound customDataSnapshot = null;
    protected boolean        customDataReplaced = false;

    public MachineUpgrade(final UpgradeType type) {
        this.type = type;
    }

    public void readNBT(NBTTagCompound tag) {
        customDataSnapshot = null;
        customDataReplaced = false;
    }

    public NBTTagCompound writeNBT() {
        return new NBTTagCompound();
    }

    public boolean hasCustomDataModified() {
        return true;
    }

    protected final NBTTagCompound captureCustomDataBeforeAccess(NBTTagCompound current) {
        if (customDataSnapshot == null && !customDataReplaced) {
            customDataSnapshot = current.copy();
        }
        return current;
    }

    protected final void markCustomDataReplaced(NBTTagCompound previous) {
        if (customDataSnapshot == null && !customDataReplaced) {
            customDataSnapshot = previous.copy();
        }
        customDataReplaced = true;
    }

    protected final boolean isCustomDataDirty(NBTTagCompound current) {
        if (customDataSnapshot == null && !customDataReplaced) {
            return false;
        }
        return !current.equals(customDataSnapshot);
    }

    public void addStaticModifier(final boolean stackable, final String key, final RecipeModifier modifier) {
        if (modifier != null && key != null) {
            staticModifiers.add(new StaticUpgradeModifier(stackable, key, modifier));
        }
    }

    protected void copyStaticModifiers(final MachineUpgrade source) {
        staticModifiers.addAll(source.staticModifiers);
    }

    public void collectStaticModifiers(final Map<String, RecipeModifier> target) {
        if (staticModifiers.isEmpty()) {
            return;
        }
        for (final StaticUpgradeModifier entry : staticModifiers) {
            target.put(entry.key, entry.resolve(stackSize));
        }
    }

    public static final class StaticUpgradeModifier {
        private final boolean        stackable;
        private final String         key;
        private final RecipeModifier modifier;

        public StaticUpgradeModifier(final boolean stackable, final String key, final RecipeModifier modifier) {
            this.stackable = stackable;
            this.key = key;
            this.modifier = modifier;
        }

        public RecipeModifier resolve(final int stackSize) {
            if (stackSize <= 1 || !stackable) {
                return modifier;
            }
            RecipeModifier multiplied = modifier;
            float factor = modifier.getModifier();
            for (int i = 0; i < stackSize; i++) {
                multiplied = multiplied.multiply(factor);
            }
            return multiplied;
        }
    }

    public abstract MachineUpgrade copy(ItemStack owner);

    @SideOnly(Side.CLIENT)
    public abstract List<String> getDescriptions();

    public abstract List<String> getBusGUIDescriptions();

    public void addEventHandler(Class<?> eventClass, UpgradeEventHandlerCT handler) {
        eventProcessor.computeIfAbsent(eventClass, v -> new ArrayList<>()).add(handler);
    }

    public List<UpgradeEventHandlerCT> getEventHandlers(Class<?> eventClass) {
        return eventProcessor.getOrDefault(eventClass, Collections.emptyList());
    }

    public TileUpgradeBus getParentBus() {
        return parentBus;
    }

    public MachineUpgrade setParentBus(final TileUpgradeBus parentBus) {
        this.parentBus = parentBus;
        return this;
    }

    public int incrementStackSize(int increment) {
        return stackSize += increment;
    }

    public int decrementStackSize(int decrement) {
        return stackSize -= decrement;
    }

    @ZenGetter("stackSize")
    public int getStackSize() {
        return stackSize;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final MachineUpgrade another)) {
            return false;
        }
        return type.equals((another).type);
    }
}
