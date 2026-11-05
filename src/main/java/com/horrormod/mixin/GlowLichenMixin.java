package com.horrormod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MultifaceGrowthFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultifaceGrowthFeature.class)
public class GlowLichenMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void horror$noGlowLichen(
            WorldGenLevel level,
            ChunkGenerator generator,
            RandomSource random,
            BlockPos origin,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (((MultifaceGrowthFeature) (Object) this).placeBlock() == Blocks.GLOW_LICHEN) {
            callback.setReturnValue(false);
        }
    }
}
