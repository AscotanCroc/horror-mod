package com.horrormod.mixin;

import com.horrormod.content.GlitchDrops;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class GlitchPlayerDropMixin {
    @Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
    private void horror$keepGlitch(boolean dropAll, CallbackInfo callback) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (GlitchDrops.is(player.getInventory().getSelectedItem())) {
            callback.cancel();
        }
    }
}
