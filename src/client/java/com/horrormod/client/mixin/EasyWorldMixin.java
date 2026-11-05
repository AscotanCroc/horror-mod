package com.horrormod.client.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreationUiState.class)
public class EasyWorldMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void horror$startOnEasy(CallbackInfo callback) {
        ((WorldCreationUiState) (Object) this).setDifficulty(Difficulty.EASY);
    }

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private void horror$onlyEasy(Difficulty difficulty, CallbackInfo callback) {
        if (difficulty != Difficulty.EASY) {
            callback.cancel();
            ((WorldCreationUiState) (Object) this).setDifficulty(Difficulty.EASY);
        }
    }

    @Inject(method = "getDifficulty", at = @At("HEAD"), cancellable = true)
    private void horror$reportEasy(CallbackInfoReturnable<Difficulty> callback) {
        callback.setReturnValue(Difficulty.EASY);
    }
}
