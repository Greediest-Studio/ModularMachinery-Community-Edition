package hellfirepvp.modularmachinery.common.machine.assembly;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class BuilderToolSettings {
    public static final String TAG = "modularmachinery:builder";

    private BuilderToolSettings() {}

    public static AssemblyOptions read(ItemStack stack) {
        return read(stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag(TAG) : new NBTTagCompound());
    }

    public static AssemblyOptions read(NBTTagCompound tag) {
        AssemblyOptions options = new AssemblyOptions();
        options.advanced = true;
        options.disassemble = tag.getBoolean("disassemble");
        options.useAeItems = tag.getBoolean("useAeItems");
        options.useAeFluids = tag.getBoolean("useAeFluids");
        options.craftMissing = tag.getBoolean("craftMissing");
        options.skipExisting = tag.getBoolean("skipExisting");
        options.dynamicLength = tag.hasKey("dynamicLength") ? Math.max(0, Math.min(4096, tag.getInteger("dynamicLength"))) : 1;
        options.attachmentModule = tag.getString("attachmentModule").trim();
        NBTTagCompound variables = tag.getCompoundTag("variables");
        for (String name : variables.getKeySet()) options.variables.put(name, variables.getString(name));
        return options;
    }

    public static NBTTagCompound write(AssemblyOptions options) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("disassemble", options.disassemble);
        tag.setBoolean("useAeItems", options.useAeItems);
        tag.setBoolean("useAeFluids", options.useAeFluids);
        tag.setBoolean("craftMissing", options.craftMissing);
        tag.setBoolean("skipExisting", options.skipExisting);
        tag.setInteger("dynamicLength", Math.max(0, Math.min(4096, options.dynamicLength)));
        tag.setString("attachmentModule", options.attachmentModule);
        NBTTagCompound variables = new NBTTagCompound();
        options.variables.forEach(variables::setString);
        tag.setTag("variables", variables);
        return tag;
    }

    public static void write(ItemStack stack, AssemblyOptions options) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag(TAG, write(options));
    }
}
