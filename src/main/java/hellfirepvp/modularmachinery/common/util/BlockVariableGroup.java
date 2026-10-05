package hellfirepvp.modularmachinery.common.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** 结构 elements 的一个来源组。alias 为空表示直接写出的方块。 */
public final class BlockVariableGroup {
    public final String alias;
    public final List<IBlockStateDescriptor> descriptors;
    private final int rotations;

    public BlockVariableGroup(String alias, List<IBlockStateDescriptor> descriptors) {
        this(alias, descriptors, 0);
    }

    private BlockVariableGroup(String alias, List<IBlockStateDescriptor> descriptors, int rotations) {
        this.alias = alias;
        this.descriptors = Collections.unmodifiableList(new ArrayList<>(descriptors));
        this.rotations = rotations;
    }

    public BlockVariableGroup copy(boolean rotate) {
        List<IBlockStateDescriptor> copied = new ArrayList<>();
        for (IBlockStateDescriptor desc : descriptors) copied.add(rotate ? desc.copyRotateYCCW(new AtomicBoolean()) : desc.copy());
        return new BlockVariableGroup(alias, copied, (rotations + (rotate ? 1 : 0)) % 4);
    }

    public List<IBlockStateDescriptor> selected(String selection) {
        IBlockStateDescriptor descriptor = BlockArray.BlockInformation.getDescriptor(selection);
        for (int i = 0; i < rotations; i++) descriptor = descriptor.copyRotateYCCW(new AtomicBoolean());
        return Collections.singletonList(descriptor);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BlockVariableGroup group)) return false;
        return Objects.equals(alias, group.alias) && descriptors.equals(group.descriptors) && rotations == group.rotations;
    }
    @Override
    public int hashCode() { return Objects.hash(alias, descriptors, rotations); }
}
