package com.horrormod.entity;

import com.horrormod.sound.HorrorSounds;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A weightless 2D shot. The picture is chosen on the client and changes every frame.
 */
public class GlitchSquare extends ThrowableProjectile {
    /** Three times the first hit. */
    private static final float DAMAGE = 12.0F;
    private static final int LIFE_TICKS = 80;
    private static final int BURST = 72;
    private static final float GLITCH_VOLUME = 4.0F;
    private static final float GLITCH_PITCH = 3.0F;

    public GlitchSquare(EntityType<? extends GlitchSquare> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public GlitchSquare(Level level, LivingEntity shooter) {
        super(HorrorEntities.GLITCH_SQUARE, shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(), level);
        this.setOwner(shooter);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Entity target = hit.getEntity();
        if (target instanceof Player) {
            target.hurtServer(server, server.damageSources().source(HorrorDamageTypes.PASSED_AWAY), DAMAGE);
            if (!target.isAlive() && this.getOwner() instanceof ReverseSkeleton skeleton) {
                erase(server, skeleton);
            }
            return;
        }
        erase(server, target);
    }

    public static void erase(ServerLevel level, Entity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5;
        double z = target.getZ();
        level.playSound(null, x, y, z, HorrorSounds.GLITCH_TRANSITION, SoundSource.MASTER, GLITCH_VOLUME, GLITCH_PITCH);
        level.sendParticles(HorrorParticles.GLITCH_SQUARE, x, y, z, BURST, 0.45, 0.45, 0.45, 0.18);
        target.discard();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}
