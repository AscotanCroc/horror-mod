package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import java.util.UUID;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A weightless column that flies through blocks. It is only visible as a censor bar.
 * Once the owner looks at it, it drifts sideways and down until the body is occluded.
 */
public class TallCensor extends Entity {
    private static final double BEHIND = 5.0;
    private static final double LIFT = 6.0;
    private static final double SPEED = 0.2;
    private static final double SINK = 0.075;
    private static final int SAMPLES = 6;

    private UUID ownerId;
    private boolean moving;
    private float driftYaw;

    public TallCensor(EntityType<? extends TallCensor> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static boolean appear(ServerLevel level, ServerPlayer player) {
        Vec3 spot = spawnSpot(level, player);
        if (spot == null) {
            return false;
        }
        TallCensor censor = HorrorEntities.TALL_CENSOR.create(level, EntitySpawnReason.EVENT);
        if (censor == null) {
            return false;
        }
        censor.ownerId = player.getUUID();
        censor.setPos(spot.x, spot.y, spot.z);
        return level.addFreshEntity(censor);
    }

    private static Vec3 spawnSpot(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        double hx = horizontal < 1.0E-4 ? 0.0 : look.x / horizontal;
        double hz = horizontal < 1.0E-4 ? 1.0 : look.z / horizontal;
        double x = player.getX() - hx * BEHIND;
        double z = player.getZ() - hz * BEHIND;
        double base = player.getY() + LIFT;
        for (int extra = 0; extra <= 16; extra += 2) {
            Vec3 spot = new Vec3(x, base + extra, z);
            if (anyClear(level, player, spot.x, spot.y, spot.z, 24.0)) {
                return spot;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        ServerPlayer owner = this.owner(server);
        if (owner == null || !owner.isAlive() || owner.level() != server || this.getY() < server.getMinY() - 8) {
            this.discard();
            return;
        }
        if (!anyClear(server, owner, this.getX(), this.getY(), this.getZ(), this.getBbHeight())) {
            this.discard();
            return;
        }
        if (!this.moving) {
            if (this.seen(owner)) {
                this.moving = true;
                this.driftYaw = this.getRandom().nextFloat() * 360.0F;
            }
            return;
        }
        double radians = Math.toRadians(this.driftYaw);
        this.setPos(
                this.getX() - Math.sin(radians) * SPEED,
                this.getY() - SINK,
                this.getZ() + Math.cos(radians) * SPEED
        );
    }

    private boolean seen(ServerPlayer owner) {
        for (int index = 0; index < SAMPLES; index++) {
            Vec3 point = this.sample(this.getX(), this.getY(), this.getZ(), this.getBbHeight(), index);
            if (HorrorEventActions.onScreen(owner, point)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyClear(ServerLevel level, ServerPlayer owner, double x, double y, double z, double height) {
        Vec3 eye = owner.getEyePosition();
        for (int index = 0; index < SAMPLES; index++) {
            Vec3 point = sample(x, y, z, height, index);
            BlockHitResult hit = level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            if (hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(point) <= 2.25) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 sample(double x, double y, double z, double height, int index) {
        double offset = 2.0 + index * Math.max(1.0, (height - 4.0) / (SAMPLES - 1.0));
        return new Vec3(x, y + Mth.clamp(offset, 1.0, Math.max(1.0, height - 1.0)), z);
    }

    private ServerPlayer owner(ServerLevel level) {
        if (this.ownerId == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(this.ownerId);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.ownerId != null) {
            output.putString("Owner", this.ownerId.toString());
        }
        output.putBoolean("Moving", this.moving);
        output.putFloat("Drift", this.driftYaw);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.getString("Owner").ifPresent(value -> this.ownerId = UUID.fromString(value));
        this.moving = input.getBooleanOr("Moving", false);
        this.driftYaw = input.getFloatOr("Drift", 0.0F);
    }
}
