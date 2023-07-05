package earth.terrarium.cadmus.mixins.common.chunkprotection;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.claims.AdminClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.claims.admin.ModFlags;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    // Prevent fire from spreading in protected chunks
    @Inject(method = "tick", at = @At(value = "HEAD", target = "Lnet/minecraft/world/level/block/FireBlock;tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"), cancellable = true)
    private void cadmus$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!AdminClaimHandler.getBooleanFlag(level, new ChunkPos(pos), ModFlags.FIRE_SPREAD)) {
            ci.cancel();
            return;
        }
        Pair<String, ClaimType> claim = ClaimHandler.getClaim(level, new ChunkPos(pos));
        if (claim != null && ModUtils.isAdmin(claim.getFirst())) {
            ci.cancel();
        }
    }
}
