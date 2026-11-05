package com.horrormod.entity;

import com.horrormod.sound.ReverseCreeperSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A creeper that cannot be lit with flint and steel. Its own sounds play backwards, and its blast rewrites nearby blocks.
 */
public class ReverseCreeper extends Creeper {
    private boolean burst;

    public ReverseCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.FLINT_AND_STEEL)) {
            return InteractionResult.FAIL;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ReverseCreeperSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ReverseCreeperSounds.DEATH;
    }

    /**
     * Replaces the vanilla crater. Called from the creeper explosion mixin once the fuse finishes.
     */
    public void burst() {
        if (this.burst || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.burst = true;
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ReverseCreeperSounds.EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        ReverseCreeperBurst.apply(level, this);
        ReverseCreeperBlackout.strike(level, this);
        DamageSource source = this.getLastDamageSource();
        if (source == null) {
            source = this.damageSources().generic();
        }
        LivingEntity credit = this.getKillCredit();
        this.dropFromLootTable(level, source, credit != null);
        ReverseCreeperBurst.clearItemsSoon(level, new Vec3(this.getX(), this.getY(), this.getZ()));
        this.discard();
    }

    public static boolean checkNaturalSpawn(
            EntityType<ReverseCreeper> type,
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
}
