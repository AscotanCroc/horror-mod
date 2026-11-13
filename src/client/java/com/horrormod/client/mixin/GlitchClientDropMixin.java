package com.horrormod.client.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class GlitchClientDropMixin {
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void horror$keepGlitch(LocalPlayer player, boolean dropAll, CallbackInfo callback) {
        if (GlitchDrops.is(player.getInventory().getSelectedItem())) {
            callback.cancel();
        }
    }
}
