package com.horrormod.mixin;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombiePackMixin {
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void horror$noReinforcements(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData data,
            CallbackInfoReturnable<SpawnGroupData> callback
    ) {
        Zombie zombie = (Zombie) (Object) this;
        if (zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE) != null) {
            zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
        }
    }
}
