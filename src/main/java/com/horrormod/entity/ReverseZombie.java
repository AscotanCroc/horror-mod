package com.horrormod.entity;

import com.horrormod.content.HorrorBlocks;
import com.horrormod.sound.ReverseZombieSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A zombie whose own sounds are the vanilla ones played backwards.
 */
public class ReverseZombie extends Zombie {
    public ReverseZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public Component getName() {
        return Component.literal("rot");
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public void die(DamageSource source) {
        boolean sunlight = diedInSunlight(source);
        super.die(source);
        if (sunlight || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (server.getBlockState(pos).isAir() || server.getBlockState(pos).canBeReplaced()) {
            server.setBlock(pos, HorrorBlocks.ROT.defaultBlockState(), 3);
        }
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        if (diedInSunlight(source)) {
            return;
        }
        super.dropAllDeathLoot(level, source);
    }

    @Override
    public boolean shouldDropExperience() {
        DamageSource source = this.getLastDamageSource();
        if (source != null && diedInSunlight(source)) {
            return false;
        }
        return super.shouldDropExperience();
    }

    private static boolean diedInSunlight(DamageSource source) {
        if (source.getEntity() instanceof Player || source.getDirectEntity() instanceof Player) {
            return false;
        }
        return source.is(DamageTypes.ON_FIRE);
    }

    public static boolean checkNaturalSpawn(
            EntityType<ReverseZombie> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        return ReverseMonsterSpawns.allow(level, reason, pos, random);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            SpawnGroupData data
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            ReverseSpawnCooldown.mark(level);
        }
        return result;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ReverseZombieSounds.AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReverseZombieSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReverseZombieSounds.DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return ReverseZombieSounds.STEP;
    }
}
