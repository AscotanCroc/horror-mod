package com.horrormod.entity;

import com.horrormod.event.HorrorEventActions;
import com.horrormod.world.VoidChunks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A copy of the player, standing far off on dry land and facing away.
 * It walks away once the camera nearly sees it, and it never turns its face toward the owner.
 */
public class FalsePlayer extends Avatar {
    public static final double FADE_START = 28.0;
    public static final double REMOVE_DISTANCE = 15.0;
    private static final double SCREEN_DOT = Math.cos(Math.toRadians(50.0));
    private static final double NEAR_DOT = Math.cos(Math.toRadians(58.0));
    private static final double LOST_DOT = Math.cos(Math.toRadians(66.0));

    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(
            FalsePlayer.class,
            EntityDataSerializers.RESOLVABLE_PROFILE
    );

    public static EntityType.EntityFactory<FalsePlayer> clientFactory;

    private UUID ownerId;
    private boolean walking;
    private boolean seen;
    private int strafeTicks;
    private float strafeDir = 1.0F;

    public FalsePlayer(EntityType<? extends FalsePlayer> type, Level level) {
        super(type, level);
    }

    public static FalsePlayer create(EntityType<FalsePlayer> type, Level level) {
        if (level.isClientSide() && clientFactory != null) {
            return clientFactory.create(type, level);
        }
        return new FalsePlayer(type, level);
    }

    public static boolean appear(ServerLevel level, ServerPlayer player) {
        BlockPos feet = findSpot(level, player);
        if (feet == null) {
            return false;
        }
        FalsePlayer copy = HorrorEntities.FALSE_PLAYER.create(level, EntitySpawnReason.EVENT);
        if (copy == null) {
            return false;
        }
        Vec3 spot = new Vec3(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        Vec3 away = spot.subtract(player.position());
        float yaw = (float) Mth.atan2(away.z, away.x) * Mth.RAD_TO_DEG - 90.0F;
        copy.ownerId = player.getUUID();
        copy.setPos(spot.x, spot.y, spot.z);
        copy.faceAway(player);
        copy.setYRot(yaw);
        copy.getEntityData().set(DATA_PROFILE, player.getProfile());
        copy.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, player.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        copy.setMainArm(player.getMainArm());
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.isArmor() || slot.getType() == EquipmentSlot.Type.HAND) {
                copy.setItemSlot(slot, player.getItemBySlot(slot).copy());
            }
        }
        if (!level.addFreshEntity(copy)) {
            return false;
        }
        copy.setHealth(copy.getMaxHealth());
        return true;
    }

    private static BlockPos findSpot(ServerLevel level, ServerPlayer player) {
        int limit = tickBlocks(level);
        int far = limit - 12;
        int near = Math.max(48, (int) (limit * 0.62));
        if (far < near + 8) {
            return null;
        }
        var random = level.getRandom();
        for (int attempt = 0; attempt < 64; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = near + random.nextDouble() * (far - near);
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            if (!level.hasChunk(x >> 4, z >> 4) || VoidChunks.isRemoved(level, x >> 4, z >> 4)) {
                continue;
            }
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, surface, z);
            if (!dry(level, feet)) {
                continue;
            }
            Vec3 eyes = new Vec3(x + 0.5, surface + 1.62, z + 0.5);
            if (HorrorEventActions.lookDot(player, eyes) >= SCREEN_DOT) {
                continue;
            }
            if (!HorrorEventActions.clearLine(player, eyes)) {
                continue;
            }
            return feet;
        }
        return null;
    }

    private static boolean dry(ServerLevel level, BlockPos feet) {
        BlockPos ground = feet.below();
        BlockState floor = level.getBlockState(ground);
        if (!floor.isFaceSturdy(level, ground, Direction.UP) || !floor.getFluidState().isEmpty()) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty() || !level.getBlockState(feet).isAir()) {
            return false;
        }
        if (!level.getBlockState(feet.above()).isAir()) {
            return false;
        }
        return level.canSeeSky(feet);
    }

    static int tickBlocks(ServerLevel level) {
        var players = level.getServer().getPlayerList();
        int view = players.getViewDistance() * 16;
        int simulation = players.getSimulationDistance() * 16;
        return Math.min(view, simulation);
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
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel server) {
            ServerPlayer owner = this.owner(server);
            if (owner == null || !owner.isAlive() || owner.level() != server) {
                this.discard();
                return;
            }
            this.faceAway(owner);
            if (this.shouldLeave(server, owner)) {
                this.discard();
                return;
            }
            this.steer(owner);
            super.aiStep();
            this.faceAway(owner);
            return;
        }
        super.aiStep();
    }

    private boolean shouldLeave(ServerLevel level, ServerPlayer owner) {
        double distance = this.distanceTo(owner);
        if (distance <= REMOVE_DISTANCE || distance > tickBlocks(level)) {
            return true;
        }
        double dot = this.bestDot(owner);
        boolean clear = dot >= -1.0;
        if (!this.walking) {
            if (clear && dot >= NEAR_DOT) {
                this.walking = true;
            }
            return false;
        }
        if (!clear || dot < LOST_DOT) {
            return true;
        }
        if (dot >= SCREEN_DOT) {
            this.seen = true;
        }
        return this.seen && dot < SCREEN_DOT;
    }

    /** The strongest look toward any unobstructed point on the body, or below -1 when the body is hidden. */
    private double bestDot(ServerPlayer owner) {
        double best = -2.0;
        Vec3 feet = this.position();
        Vec3[] points = {
                feet.add(0.0, 0.4, 0.0),
                this.getEyePosition(),
                feet.add(0.0, 1.7, 0.0)
        };
        for (Vec3 point : points) {
            if (!HorrorEventActions.clearLine(owner, point)) {
                continue;
            }
            best = Math.max(best, HorrorEventActions.lookDot(owner, point));
        }
        return best;
    }

    private void steer(ServerPlayer owner) {
        if (!this.walking) {
            this.zza = 0.0F;
            this.xxa = 0.0F;
            return;
        }
        if (this.horizontalCollision && this.onGround() && this.stepUp(owner)) {
            this.jumpFromGround();
            this.zza = 1.0F;
            this.xxa = 0.0F;
            return;
        }
        if (this.horizontalCollision && this.onGround()) {
            if (this.strafeTicks <= 0) {
                this.strafeDir = -this.strafeDir;
                this.strafeTicks = 10;
            }
            this.xxa = this.strafeDir;
            this.zza = 0.35F;
            this.strafeTicks--;
            return;
        }
        this.zza = 1.0F;
        this.xxa = 0.0F;
    }

    private boolean stepUp(ServerPlayer owner) {
        Vec3 away = this.position().subtract(owner.position());
        double horizontal = Math.sqrt(away.x * away.x + away.z * away.z);
        if (horizontal < 1.0E-4) {
            return false;
        }
        BlockPos ahead = BlockPos.containing(this.getX() + away.x / horizontal, this.getY(), this.getZ() + away.z / horizontal);
        return this.blocked(ahead) && !this.blocked(ahead.above()) && !this.blocked(ahead.above(2));
    }

    private boolean blocked(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(this.level(), pos).isEmpty();
    }

    private void faceAway(ServerPlayer owner) {
        Vec3 away = this.position().subtract(owner.position());
        double horizontal = Math.sqrt(away.x * away.x + away.z * away.z);
        if (horizontal < 1.0E-4) {
            return;
        }
        float yaw = (float) (Mth.atan2(away.z, away.x) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setYHeadRot(yaw);
        this.yHeadRotO = yaw;
        this.setYBodyRot(yaw);
        this.yBodyRotO = yaw;
        this.setXRot(0.0F);
        this.xRotO = 0.0F;
    }

    private ServerPlayer owner(ServerLevel level) {
        if (this.ownerId == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(this.ownerId);
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
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerId != null) {
            output.putString("Owner", this.ownerId.toString());
        }
        output.putBoolean("Walking", this.walking);
        output.putBoolean("Seen", this.seen);
        output.putInt("StrafeTicks", this.strafeTicks);
        output.putFloat("Strafe", this.strafeDir);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Owner").ifPresent(value -> this.ownerId = UUID.fromString(value));
        this.walking = input.getBooleanOr("Walking", false);
        this.seen = input.getBooleanOr("Seen", false);
        this.strafeTicks = input.getIntOr("StrafeTicks", 0);
        this.strafeDir = input.getFloatOr("Strafe", 1.0F);
    }
}
