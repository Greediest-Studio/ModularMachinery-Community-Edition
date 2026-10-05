package hellfirepvp.modularmachinery.common.machine.assembly;

import java.util.LinkedHashMap;
import java.util.Map;

/** 一次任务的选项快照，不持有工具或客户端状态。 */
public final class AssemblyOptions {
    public net.minecraft.util.EnumHand hand = net.minecraft.util.EnumHand.MAIN_HAND;
    public boolean advanced;
    public boolean disassemble;
    public boolean useAeItems;
    public boolean useAeFluids;
    public boolean craftMissing;
    public boolean skipExisting;
    public int dynamicLength = 1;
    public String attachmentModule = "";
    public final Map<String, String> variables = new LinkedHashMap<>();

    public int tickInterval() {
        return advanced ? AssemblyConfig.advancedTickInterval : AssemblyConfig.tickBlock;
    }

    public int operationsPerTick() {
        return advanced ? AssemblyConfig.advancedOperationsPerTick : 1;
    }

    public static AssemblyOptions simple(int length) {
        AssemblyOptions options = new AssemblyOptions();
        options.dynamicLength = length;
        return options;
    }
}
