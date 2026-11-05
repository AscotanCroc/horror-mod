package com.horrormod.entity;

import com.horrormod.world.VoidChunks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Puts reverse monsters on the surface near each player during the night.
 * Ordinary monster spawns rarely reach the surface, because cave mobs fill the cap first.
 */
public final class ReverseZombieNights {
    private static final int NIGHT_START = 13000;
    private static final int NIGHT_END = 23000;
    private static final int ATTEMPTS = 12;
    private final Map<UUID, NightProgress> spawnedNight = new HashMap<>();

    private ReverseZombieNights() {
    }

    public static void initialize() {
        ReverseZombieNights nights = new ReverseZombieNights();
        ServerTickEvents.END_LEVEL_TICK.register(nights::tick);
    }

    private void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % 20L != 0L) {
            return;
        }
        if (level.getServer().getWorldData().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (!level.getGameRules().get(GameRules.SPAWN_MOBS) || !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) {
            return;
        }
        long time = level.getOverworldClockTime();
        int dayTime = Math.floorMod((int) time, 24000);
        if (dayTime < NIGHT_START || dayTime >= NIGHT_END) {
            return;
        }
        long nightId = Math.floorDiv(time - NIGHT_START, 24000L);
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !onSurface(level, player)) {
                continue;
            }
            NightProgress progress = spawnedNight.computeIfAbsent(player.getUUID(), id -> new NightProgress());
            if (progress.nightId != nightId) {
                progress.nightId = nightId;
                progress.spawned = false;
                progress.spawnTonight = level.getRandom().nextBoolean();
                progress.kind = level.getRandom().nextInt(3);
            }
            if (!progress.spawnTonight || progress.spawned) {
                continue;
            }
            NightKind kind = NightKind.values()[progress.kind];
            int spawned = fillNight(level, player, 0, 1, kind.type, kind.entityClass, kind.airBlocks);
            progress.spawned = spawned > 0;
        }
    }

    private static int fillNight(
            ServerLevel level,
            ServerPlayer player,
            int spawned,
            int wanted,
            EntityType<?> type,
            Class<? extends Entity> kind,
            int airBlocks
    ) {
        if (spawned >= wanted || ReverseSpawnCooldown.isBlocked(level)) {
            return spawned;
        }
        int nearby = level.getEntities(
                EntityTypeTest.forClass(kind),
                player.getBoundingBox().inflate(40.0, 12.0, 40.0),
                entity -> true
        ).size();
        if (nearby >= wanted) {
            return wanted;
        }
        return spawnNear(level, player, type, airBlocks) ? spawned + 1 : spawned;
    }

    private static boolean onSurface(ServerLevel level, ServerPlayer player) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getBlockX(), player.getBlockZ());
        double above = player.getY() - surface;
        return above >= -6.0 && above <= 12.0;
    }

    private static boolean spawnNear(ServerLevel level, ServerPlayer player, EntityType<?> type, int airBlocks) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * (Math.PI * 2.0);
            int distance = 16 + random.nextInt(13);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            if (!level.isLoaded(new BlockPos(x, player.getBlockY(), z))) {
                continue;
            }
            if (VoidChunks.isRemoved(level, x >> 4, z >> 4)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (Math.abs(y - player.getY()) > 8.0) {
                continue;
            }
            BlockPos feet = new BlockPos(x, y, z);
            BlockPos ground = feet.below();
            if (!hasRoom(level, feet, airBlocks)) {
                continue;
            }
            if (!level.getFluidState(feet).isEmpty() || !level.getBlockState(ground).getFluidState().isEmpty()) {
                continue;
            }
            if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
                continue;
            }
            return type.spawn(level, feet, EntitySpawnReason.NATURAL) != null;
        }
        return false;
    }

    private static boolean hasRoom(ServerLevel level, BlockPos feet, int airBlocks) {
        for (int offset = 0; offset < airBlocks; offset++) {
            if (!level.getBlockState(feet.above(offset)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private enum NightKind {
        ZOMBIE(HorrorEntities.REVERSE_ZOMBIE, ReverseZombie.class, 2),
        ENDERMAN(HorrorEntities.REVERSE_ENDERMAN, ReverseEnderman.class, 3),
        CREEPER(HorrorEntities.REVERSE_CREEPER, ReverseCreeper.class, 2);

        private final EntityType<?> type;
        private final Class<? extends Entity> entityClass;
        private final int airBlocks;

        NightKind(EntityType<?> type, Class<? extends Entity> entityClass, int airBlocks) {
            this.type = type;
            this.entityClass = entityClass;
            this.airBlocks = airBlocks;
        }
    }

    private static final class NightProgress {
        private long nightId = Long.MIN_VALUE;
        private boolean spawnTonight;
        private boolean spawned;
        private int kind;
    }
}
