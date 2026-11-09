package com.horrormod.mixin;

import com.horrormod.entity.ReverseCreeperDark;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public class CreeperLightMixin {
    @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true)
    private void horror$snuffNearCreeper(long packed, BlockState state, CallbackInfoReturnable<Integer> callback) {
        if (callback.getReturnValue() <= 0) {
            return;
        }
        BlockGetter view = ((HorrorLightEngineAccessor) this).horror$chunkSource().getLevel();
        if (!(view instanceof Level level)) {
            return;
        }
        if (ReverseCreeperDark.snuffs(level, BlockPos.of(packed))) {
            callback.setReturnValue(0);
        }
    }
}
