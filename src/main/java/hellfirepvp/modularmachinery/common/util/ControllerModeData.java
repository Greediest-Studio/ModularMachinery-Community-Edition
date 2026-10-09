package hellfirepvp.modularmachinery.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

/** 控制器的独立模式存档，按机器注册名隔离；同步访问供异步配方检查使用。 */
public final class ControllerModeData {
    private NBTTagCompound values = new NBTTagCompound();
    private volatile long version;

    public synchronized int getValue(ResourceLocation machine, ControllerMode mode) {
        NBTTagCompound machineValues = values.getCompoundTag(machine.toString());
        if (machineValues.hasKey(mode.getName(), Constants.NBT.TAG_INT)) {
            int value = machineValues.getInteger(mode.getName());
            if (mode.getModes().containsKey(value)) {
                return value;
            }
        }
        // 新机器或脚本删除了原选项时使用当前默认值。
        return mode.getDefaultValue();
    }

    public synchronized boolean setValue(ResourceLocation machine, ControllerMode mode, int value) {
        if (!mode.getModes().containsKey(value) || getValue(machine, mode) == value) {
            return false;
        }
        String machineName = machine.toString();
        NBTTagCompound machineValues = values.getCompoundTag(machineName);
        machineValues.setInteger(mode.getName(), value);
        values.setTag(machineName, machineValues);
        version++;
        return true;
    }

    public synchronized NBTTagCompound serialize() {
        return values.copy();
    }

    public synchronized void deserialize(NBTTagCompound tag) {
        if (!values.equals(tag)) {
            values = tag.copy();
            version++;
        }
    }

    public long getVersion() {
        return version;
    }
}
