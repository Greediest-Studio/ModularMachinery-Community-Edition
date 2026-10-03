// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.command;

import hellfirepvp.modularmachinery.common.integration.nuclearcraft.ScrubbedChunksCache;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandGetCacheInfo extends CommandBase {

    @Override
    public String getName() {
        return "mm-scrubber-info";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.modularmachinery.scrubber_info.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(new TextComponentString(ScrubbedChunksCache.getInformation()));
    }
}
