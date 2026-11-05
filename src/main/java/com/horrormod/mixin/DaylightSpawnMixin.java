package com.horrormod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public class DaylightSpawnMixin {
    @Inject(method = "isDarkEnoughToSpawn", at = @At("HEAD"), cancellable = true)
    private static void horror$useDaylight(ServerLevelAccessor level, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> callback) {
        if (level.getLevel().dimension() != Level.OVERWORLD) {
            return;
        }
        int sky = level.getBrightness(LightLayer.SKY, pos);
        if (sky > random.nextInt(32)) {
            callback.setReturnValue(false);
            return;
        }
        DimensionType dimension = level.dimensionType();
        int blockLimit = dimension.monsterSpawnBlockLightLimit();
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        if (blockLimit < 15 && block > blockLimit) {
            callback.setReturnValue(false);
            return;
        }
        int daylight = Math.max(block, sky);
        callback.setReturnValue(daylight <= dimension.monsterSpawnLightTest().sample(random));
    }
}
