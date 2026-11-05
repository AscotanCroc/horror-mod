package com.horrormod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bat.class)
public class BatSpawnMixin {
    @Inject(method = "checkBatSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void horror$noBats(
            EntityType<Bat> type,
            LevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> callback
    ) {
        callback.setReturnValue(false);
    }
}
