package hellfirepvp.modularmachinery.common.tiles;

import net.minecraft.nbt.NBTTagCompound;

public interface SettingsTransfer {
    NBTTagCompound downloadSettings();

    void uploadSettings(NBTTagCompound settings);
}
