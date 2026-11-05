package com.horrormod.mixin;

import com.horrormod.entity.ReverseZombie;
import com.horrormod.sound.ReverseZombieSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.zombie.Zombie$ZombieAttackTurtleEggGoal")
public class ReverseZombieEggMixin {
    @Shadow
    @Final
    private Zombie this$0;

    @Inject(method = "playDestroyProgressSound", at = @At("HEAD"), cancellable = true)
    private void horror$reverseEggSound(LevelAccessor level, BlockPos pos, CallbackInfo callback) {
        if (!(this.this$0 instanceof ReverseZombie)) {
            return;
        }
        level.playSound(null, pos, ReverseZombieSounds.DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + this.this$0.getRandom().nextFloat() * 0.2F);
        callback.cancel();
    }
}
