package com.horrormod.mixin;

import com.horrormod.world.AbandonedHouseLoot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainer.class)
public interface AbandonedHouseLootMixin {
    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void horror$noteHouse(Player player, CallbackInfo callback) {
        AbandonedHouseLoot.note(((RandomizableContainer) this).getLootTable());
    }

    @Inject(method = "unpackLootTable", at = @At("RETURN"))
    private void horror$scatterHouse(Player player, CallbackInfo callback) {
        if (!AbandonedHouseLoot.consumeNote() || !(this instanceof Container contents)) {
            return;
        }
        RandomSource random = player != null && player.level() instanceof ServerLevel level
                ? level.getRandom()
                : RandomSource.create();
        AbandonedHouseLoot.arrange(contents, random);
    }
}
