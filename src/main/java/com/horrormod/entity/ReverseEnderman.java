package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import com.horrormod.sound.ReverseEndermanSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * An enderman whose own sounds are the vanilla ones played backwards.
 */
public class ReverseEnderman extends Enderman {
    private boolean vanished;

    public ReverseEnderman(EntityType<? extends Enderman> type, Level level) {
        super(type, level);
    }

    @Override
    public Component getName() {
        return Component.literal("tainted");
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.isAlive() && inSunlight(this)) {
            if (this.level() instanceof ServerLevel server) {
                HorrorEventActions.flickerIfSeen(server, this);
            }
            this.discard();
            return;
        }
        super.aiStep();
    }

    /** True when the sky is bright and open above the mob's feet or eyes. */
    public static boolean inSunlight(LivingEntity entity) {
        Level level = entity.level();
        if (!level.isBrightOutside()) {
            return false;
        }
        if (level.canSeeSky(entity.blockPosition())) {
            return true;
        }
        return level.canSeeSky(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
    }

    public static boolean checkNaturalSpawn(
            EntityType<ReverseEnderman> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        return ReverseMonsterSpawns.allow(level, reason, pos, random, type);
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
            ReverseRoster.noteSpawn(level, this.getType());
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        if (this.vanished || this.isRemoved()) {
            return;
        }
        this.vanished = true;
        if (this.level() instanceof ServerLevel server) {
            HorrorEventActions.flickerIfSeen(server, this);
            ReverseRewards.queue(this, source);
        }
        this.discard();
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        super.playSound(sound, volume * 2.8F, Math.min(2.3F, pitch * 1.65F));
    }

    @Override
    public void makeSound(SoundEvent sound) {
        if (this.isDeadOrDying() || this.vanished) {
            return;
        }
        super.makeSound(sound);
    }

    @Override
    public void handleDamageEvent(DamageSource source) {
        if (this.isDeadOrDying() || this.getHealth() <= 0.0F || this.vanished) {
            return;
        }
        super.handleDamageEvent(source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isCreepy() ? ReverseEndermanSounds.SCREAM : ReverseEndermanSounds.AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReverseEndermanSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReverseEndermanSounds.DEATH;
    }
}
