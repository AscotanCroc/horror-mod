package com.horrormod.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A point above a reverse mob. The client draws it through walls.
 */
public class ReverseMarker extends Entity {
    private UUID targetId;

    public ReverseMarker(EntityType<? extends ReverseMarker> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** @return how many living reverse mobs were found */
    public static int markAll(MinecraftServer server) {
        int found = 0;
        for (ServerLevel level : server.getAllLevels()) {
            Set<UUID> marked = new HashSet<>();
            List<Entity> mobs = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ReverseMarker marker && marker.targetId != null) {
                    marked.add(marker.targetId);
                } else if (entity.isAlive() && HorrorEntities.isReverse(entity)) {
                    mobs.add(entity);
                }
            }
            found += mobs.size();
            for (Entity mob : mobs) {
                if (marked.contains(mob.getUUID())) {
                    continue;
                }
                ReverseMarker marker = HorrorEntities.REVERSE_MARKER.create(level, EntitySpawnReason.EVENT);
                if (marker == null) {
                    continue;
                }
                marker.targetId = mob.getUUID();
                marker.snapTo(mob);
                level.addFreshEntity(marker);
            }
        }
        return found;
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
        Entity target = this.targetId == null ? null : server.getEntity(this.targetId);
        if (target == null || !target.isAlive() || !HorrorEntities.isReverse(target)) {
            this.discard();
            return;
        }
        this.snapTo(target);
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    private void snapTo(Entity target) {
        this.setPos(target.getX(), target.getY() + target.getBbHeight() + 0.45, target.getZ());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
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

    /** The box is tiny, so the normal distance check would hide the icon nearby. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.targetId != null) {
            output.putString("Target", this.targetId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.getString("Target").ifPresent(value -> this.targetId = UUID.fromString(value));
    }
}
