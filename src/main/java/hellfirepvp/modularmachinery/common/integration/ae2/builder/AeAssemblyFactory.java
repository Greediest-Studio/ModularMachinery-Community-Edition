package hellfirepvp.modularmachinery.common.integration.ae2.builder;

import hellfirepvp.modularmachinery.common.machine.assembly.AssemblyOptions;
import hellfirepvp.modularmachinery.common.machine.assembly.MachineAssembly;
import hellfirepvp.modularmachinery.common.util.StructureIngredient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** 隔离字节码校验对 AE 任务继承关系的解析，无 AE 时不会加载此类。 */
public final class AeAssemblyFactory {
    private AeAssemblyFactory() {}

    public static MachineAssembly create(World world, BlockPos pos, EntityPlayer player,
                                         StructureIngredient ingredient, AssemblyOptions options) {
        return new AeMachineAssembly(world, pos, player, ingredient, options);
    }
}
