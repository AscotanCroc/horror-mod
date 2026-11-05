package com.horrormod.entity;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A walking copy of the player who threw the null pointer.
 * It faces away at first, then dies if that player gets close or looks at its face.
 */
public class NullClone extends Avatar {
    public static final double CLOSE_DISTANCE = 3.0;
    private static final double FACE_RADIUS = 0.35;
    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(
            NullClone.class,
            EntityDataSerializers.RESOLVABLE_PROFILE
    );

    public static EntityType.EntityFactory<NullClone> clientFactory;

    private UUID ownerId;
    private boolean announced;

    public NullClone(EntityType<? extends NullClone> type, Level level) {
        super(type, level);
    }

    public static NullClone create(EntityType<NullClone> type, Level level) {
        if (level.isClientSide() && clientFactory != null) {
            return clientFactory.create(type, level);
        }
        return new NullClone(type, level);
    }

    public static void appear(ServerLevel level, ThrownNullPointer pearl) {
        if (!(pearl.getOwner() instanceof ServerPlayer player)) {
            return;
        }
        NullClone clone = HorrorEntities.NULL_CLONE.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (clone == null) {
            return;
        }
        Vec3 landing = pearl.position();
        Vec3 away = landing.subtract(player.position());
        float yaw = (float) Mth.atan2(away.z, away.x) * Mth.RAD_TO_DEG - 90.0F;
        clone.ownerId = player.getUUID();
        clone.setPos(landing.x, landing.y, landing.z);
        clone.setYRot(yaw);
        clone.setYHeadRot(yaw);
        clone.setYBodyRot(yaw);
        clone.getEntityData().set(DATA_PROFILE, player.getProfile());
        clone.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, player.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        clone.setMainArm(player.getMainArm());
        level.addFreshEntity(clone);
        clone.setHealth(clone.getMaxHealth());
        clone.hurtServer(level, level.damageSources().enderPearl(), 5.0F);
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.isArmor() || slot.getType() == EquipmentSlot.Type.HAND) {
                clone.setItemSlot(slot, player.getItemBySlot(slot).copy());
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFILE, ResolvableProfile.createUnresolved("Player"));
    }

    @Override
    public ResolvableProfile getProfile() {
        return this.getEntityData().get(DATA_PROFILE);
    }

    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.isAlive()) {
            this.zza = 1.0F;
            if (this.getRandom().nextInt(40) == 0) {
                this.setYRot(this.getYRot() + (this.getRandom().nextFloat() - 0.5F) * 60.0F);
                this.setYHeadRot(this.getYRot());
            }
            if (this.horizontalCollision && this.onGround()) {
                this.jumpFromGround();
            }
            this.watchOwner();
        }
        super.aiStep();
    }

    private void watchOwner() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        ServerPlayer owner = this.owner(server);
        if (owner == null || owner.level() != server || !this.isAlive()) {
            return;
        }
        if (this.distanceTo(owner) < CLOSE_DISTANCE || this.ownerLooksAtFace(owner)) {
            this.hurtServer(server, server.damageSources().generic(), this.getHealth());
        }
    }

    private boolean ownerLooksAtFace(ServerPlayer owner) {
        Vec3 look = owner.getViewVector(1.0F);
        Vec3 eyes = owner.getEyePosition();
        Vec3 face = this.getEyePosition();
        Vec3 toFace = face.subtract(eyes);
        double along = toFace.dot(look);
        if (along <= 0.0) {
            return false;
        }
        Vec3 closest = eyes.add(look.scale(along));
        return closest.distanceToSqr(face) <= FACE_RADIUS * FACE_RADIUS;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.announced && this.level() instanceof ServerLevel server) {
            this.announced = true;
            this.announce(server);
        }
        super.die(source);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        ServerPlayer owner = this.owner(level);
        if (owner == null) {
            return;
        }
        var inventory = owner.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack.copy());
            }
        }
        ExperienceOrb.award(level, this.position(), this.experienceFor(level, owner));
    }

    private int experienceFor(ServerLevel level, Player owner) {
        if (owner.isSpectator() || level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return 0;
        }
        return Math.min(owner.experienceLevel * 7, 100);
    }

    private void announce(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
            return;
        }
        ServerPlayer owner = this.owner(level);
        String name = owner == null ? "Player" : owner.getName().getString();
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(name + " has java.lang.NullPointerException: Loading entity in world"),
                false
        );
    }

    private ServerPlayer owner(ServerLevel level) {
        if (this.ownerId == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(this.ownerId);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerId != null) {
            output.putString("Owner", this.ownerId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Owner").ifPresent(value -> this.ownerId = UUID.fromString(value));
    }
}
