package com.horrormod.entity;

import com.horrormod.content.BlackCobwebBlock;
import com.horrormod.content.HorrorBlocks;
import com.horrormod.network.SpiderSnapPayload;
import com.horrormod.sound.ReverseSpiderSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A slightly faster spider. Its sounds are the vanilla ones played backwards, stacked, and cut short.
 */
public class ReverseSpider extends Spider {
    /** Vanilla spiders move at 0.3. */
    private static final double MOVEMENT_SPEED = 0.36;
    private static final int SPREAD_RADIUS = 7;
    private boolean burst;
    private int echoTicks;
    private SoundEvent echo;
    private float echoVolume;
    private float echoPitch;

    public ReverseSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 8;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.isAlive() && ReverseEnderman.inSunlight(this)) {
            this.die(this.damageSources().generic());
            return;
        }
        super.aiStep();
        if (!this.level().isClientSide() && this.isAlive() && this.tickCount % 20 == 0) {
            this.playAmbientSound();
        }
        if (this.echoTicks > 0 && --this.echoTicks == 0 && this.echo != null && !this.level().isClientSide()) {
            burst(this.echo, 3 + this.getRandom().nextInt(3), this.echoVolume, this.echoPitch);
        }
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (this.level().isClientSide() || sound == null) {
            return;
        }
        float loud = Math.max(volume, 0.2F) * 2.5F;
        float fast = Math.min(2.0F, pitch * 2.0F);
        burst(sound, 4 + this.getRandom().nextInt(3), loud, fast);
        this.echo = sound;
        this.echoVolume = loud;
        this.echoPitch = fast;
        this.echoTicks = 2 + this.getRandom().nextInt(3);
    }

    private void burst(SoundEvent sound, int copies, float volume, float pitch) {
        for (int copy = 0; copy < copies; copy++) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), volume, pitch);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.burst) {
            return;
        }
        this.burst = true;
        if (this.level() instanceof ServerLevel level) {
            spread(level);
            float speed = 0.5F + level.getRandom().nextFloat() * 1.9F;
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ReverseSpiderSounds.KILL, SoundSource.MASTER, 1.6F, speed);
            level.sendParticles(
                    HorrorParticles.BLACK_SQUARE,
                    this.getX(),
                    this.getY() + this.getBbHeight() * 0.5,
                    this.getZ(),
                    160,
                    0.2,
                    0.2,
                    0.2,
                    0.85
            );
            snapKiller(source);
        }
        super.die(source);
        this.discard();
    }

    private void snapKiller(DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, SpiderSnapPayload.INSTANCE);
        }
    }

    private void spread(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        RandomSource random = level.getRandom();
        List<BlockPos> wave = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        wave.add(origin);
        seen.add(origin);
        int placed = 0;
        while (!wave.isEmpty() && placed < 70) {
            BlockPos pos = wave.remove(random.nextInt(wave.size()));
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!seen.add(next) || origin.distSqr(next) > SPREAD_RADIUS * SPREAD_RADIUS) {
                    continue;
                }
                if (!level.isLoaded(next) || !level.getBlockState(next).isAir()) {
                    continue;
                }
                wave.add(next);
                if (random.nextFloat() < 0.62F) {
                    level.setBlock(next, HorrorBlocks.BLACK_COBWEB.defaultBlockState()
                            .setValue(BlackCobwebBlock.AGE, 0)
                            .setValue(BlackCobwebBlock.WAVE, BlackCobwebBlock.wave(origin, next)), 3);
                    level.scheduleTick(next, HorrorBlocks.BLACK_COBWEB, 1);
                    placed++;
                }
            }
        }
    }

    public static boolean checkNaturalSpawn(
            EntityType<ReverseSpider> type,
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
        return ReverseSpiderSounds.AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReverseSpiderSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReverseSpiderSounds.DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ReverseSpiderSounds.STEP, 0.15F, 1.0F);
    }
}
