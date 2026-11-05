package com.horrormod.mixin;

import com.horrormod.world.VoidChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class VoidChunkWriteMixin {
    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    private void horror$keepVoidEmpty(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> callback) {
        ServerLevelAccessor region = (ServerLevelAccessor) (Object) this;
        if (VoidChunks.isRemoved(region.getLevel(), pos.getX() >> 4, pos.getZ() >> 4)) {
            callback.setReturnValue(false);
        }
    }
}
