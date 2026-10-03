package hellfirepvp.modularmachinery.mixin.nuclearcraft;

import hellfirepvp.modularmachinery.common.integration.nuclearcraft.InterdimensionalChunkPos;
import hellfirepvp.modularmachinery.common.integration.nuclearcraft.ScrubbedChunksCache;
import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadiationHelper;
import nc.radiation.WorldRadiationHandler;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NCO 2o.9.6：只跳过被净化的区块，不取消整个世界的更新。 */
@Mixin(value = WorldRadiationHandler.class, remap = false)
public class RadiationHandlerMixin {
    @Redirect(method = "updateInternal", at = @At(value = "INVOKE",
        target = "Lnc/radiation/RadiationHelper;getRadiationSource(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)Lnc/capability/radiation/source/IRadiationSource;"))
    private IRadiationSource getUnscrubbedSource(ICapabilityProvider provider) {
        IRadiationSource source = RadiationHelper.getRadiationSource(provider);
        if (source != null && provider instanceof Chunk chunk && isScrubbed(chunk)) {
            source.setRadiationLevel(0);
            source.setRadiationBuffer(0);
            // NCO 遇到空辐射源会继续处理下一个区块。
            return null;
        }
        return source;
    }

    @Redirect(method = "updateInternal", at = @At(value = "INVOKE",
        target = "Lnc/radiation/RadiationHelper;spreadRadiationFromChunk(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/chunk/Chunk;)V"))
    private void spreadOutsideScrubbedArea(Chunk from, Chunk to) {
        if (!isScrubbed(from) && !isScrubbed(to)) RadiationHelper.spreadRadiationFromChunk(from, to);
    }

    private static boolean isScrubbed(Chunk chunk) {
        return chunk != null && ScrubbedChunksCache.isChunkScrubbed(
            InterdimensionalChunkPos.of(chunk.getWorld().provider.getDimension(), chunk.getPos()));
    }
}
