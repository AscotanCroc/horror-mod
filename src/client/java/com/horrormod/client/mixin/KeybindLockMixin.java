package com.horrormod.client.mixin;

import com.horrormod.client.event.HorrorClientEffects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class KeybindLockMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void horror$freezeKeys(CallbackInfo callback) {
        if (!HorrorClientEffects.lockingControls()) {
            return;
        }
        for (KeyMapping mapping : ((Minecraft) (Object) this).options.keyMappings) {
            while (mapping.consumeClick()) {
                // Drop presses that happened while the player cannot act.
            }
        }
        callback.cancel();
    }
}
