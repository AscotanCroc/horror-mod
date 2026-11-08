package com.horrormod.entity;

import com.horrormod.sound.ReverseSkeletonSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A skeleton that walks and shoots slowly. Its sounds are the vanilla ones played backwards,
 * and the shot is a weightless glitch square instead of an arrow.
 */
public class ReverseSkeleton extends Skeleton {
    /** Vanilla skeletons move at 0.25. */
    private static final double MOVEMENT_SPEED = 0.08;
    /** Vanilla hard interval is 20 ticks. */
    private static final int HARD_ATTACK_INTERVAL = 60;
    /** Vanilla interval is 40 ticks. */
    private static final int ATTACK_INTERVAL = 120;
    private static final float SHOT_SPEED = 1.0F / 3.0F;

    public ReverseSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
    }

    public static boolean checkNaturalSpawn(
            EntityType<ReverseSkeleton> type,
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
    public void aiStep() {
        if (!this.level().isClientSide() && this.isAlive() && ReverseEnderman.inSunlight(this)) {
            if (this.level() instanceof ServerLevel server) {
                GlitchSquare.erase(server, this);
            }
            return;
        }
        super.aiStep();
    }

    @Override
    protected int getHardAttackInterval() {
        return HARD_ATTACK_INTERVAL;
    }

    @Override
    protected int getAttackInterval() {
        return ATTACK_INTERVAL;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        GlitchSquare square = new GlitchSquare(this.level(), this);
        double dx = target.getX() - square.getX();
        double dy = target.getY(0.45) - square.getY();
        double dz = target.getZ() - square.getZ();
        Projectile.spawnProjectileUsingShoot(
                square,
                server,
                ItemStack.EMPTY,
                dx,
                dy,
                dz,
                SHOT_SPEED,
                spread(server.getDifficulty())
        );
        this.playSound(ReverseSkeletonSounds.SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static float spread(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 10.0F;
            case NORMAL -> 6.0F;
            case HARD -> 2.0F;
            default -> 14.0F;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ReverseSkeletonSounds.AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReverseSkeletonSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReverseSkeletonSounds.DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return ReverseSkeletonSounds.STEP;
    }
}
