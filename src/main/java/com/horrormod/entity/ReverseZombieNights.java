package com.horrormod.entity;

import com.horrormod.HorrorMod;
import com.horrormod.world.VoidChunks;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Places one reverse monster into an empty slot.
 * The roll happens before a kind is chosen, so a locked kind does not make the others rarer.
 * Underground is the common roll. A thunderstorm uses that roll on the surface too.
 */
public final class ReverseZombieNights {
    private static final int ATTEMPT_INTERVAL = 200;
    private static final int ATTEMPTS = 32;

    private ReverseZombieNights() {
    }

    public static void initialize() {
        ReverseZombieNights nights = new ReverseZombieNights();
        ServerTickEvents.END_LEVEL_TICK.register(nights::tick);
    }

    private void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % ATTEMPT_INTERVAL != 0L) {
            return;
        }
        if (level.getServer().getWorldData().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (!level.getGameRules().get(GameRules.SPAWN_MOBS) || !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) {
            return;
        }
        if (!ReverseMonsterSpawns.firstNightStarted(level) || ReverseSpawnCooldown.isBlocked(level)) {
            return;
        }
        RandomSource random = level.getRandom();
        boolean storm = ReverseMonsterSpawns.storming(level);
        List<ServerPlayer> underground = new ArrayList<>();
        List<ServerPlayer> surface = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            ReverseMonsterSpawns.Band band = ReverseMonsterSpawns.band(level, player.blockPosition());
            if (band == ReverseMonsterSpawns.Band.UNDERGROUND) {
                underground.add(player);
            } else if (band == ReverseMonsterSpawns.Band.SURFACE) {
                surface.add(player);
            }
        }
        boolean placeUnderground = !underground.isEmpty()
                && !ReverseMonsterSpawns.darkOccupied(level)
                && random.nextInt(ReverseMonsterSpawns.UNDERGROUND_CHANCE) == 0;
        boolean placeSurface = ReverseMonsterSpawns.surfaceNight(level)
                && !surface.isEmpty()
                && !ReverseMonsterSpawns.surfaceOccupied(level)
                && random.nextInt(ReverseMonsterSpawns.appearanceChance(true, storm)) == 0;
        if (placeUnderground) {
            place(level, underground.get(random.nextInt(underground.size())), false);
        }
        if (placeSurface) {
            place(level, surface.get(random.nextInt(surface.size())), true);
        }
    }

    private static void place(ServerLevel level, ServerPlayer player, boolean surface) {
        ReverseSpawnOrder.Kind kind = ReverseSpawnOrder.pick(level, level.getRandom());
        if (kind == null) {
            return;
        }
        if (surface) {
            spawnOnSurface(level, player, kind.type(), kind.airBlocks());
        } else {
            spawnUnderground(level, player, kind.type(), kind.airBlocks());
        }
    }

    private static boolean spawnOnSurface(ServerLevel level, ServerPlayer player, EntityType<?> type, int airBlocks) {
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
            BlockPos feet = new BlockPos(x, y, z);
            if (ReverseMonsterSpawns.band(level, feet) != ReverseMonsterSpawns.Band.SURFACE) {
                continue;
            }
            if (!openDarkGround(level, feet, airBlocks)) {
                continue;
            }
            return spawned(level, type, feet);
        }
        return false;
    }

    private static boolean spawnUnderground(ServerLevel level, ServerPlayer player, EntityType<?> type, int airBlocks) {
        RandomSource random = level.getRandom();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * (Math.PI * 2.0);
            int distance = 16 + random.nextInt(13);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = origin.getY() + random.nextInt(25) - 12;
            BlockPos feet = new BlockPos(x, y, z);
            if (!level.isLoaded(feet) || VoidChunks.isRemoved(level, x >> 4, z >> 4)) {
                continue;
            }
            if (ReverseMonsterSpawns.band(level, feet) != ReverseMonsterSpawns.Band.UNDERGROUND) {
                continue;
            }
            if (!openDarkGround(level, feet, airBlocks)) {
                continue;
            }
            return spawned(level, type, feet);
        }
        return false;
    }

    /**
     * An event spawn is not a biome roll, so the natural placement rule is not consulted.
     */
    private static boolean spawned(ServerLevel level, EntityType<?> type, BlockPos feet) {
        if (type.spawn(level, feet, EntitySpawnReason.EVENT) == null) {
            return false;
        }
        ReverseRoster.noteSpawn(level, type);
        HorrorMod.LOGGER.info("Spawned {} at {}", type, feet.toShortString());
        return true;
    }

    private static boolean openDarkGround(ServerLevel level, BlockPos feet, int airBlocks) {
        if (level.getBrightness(LightLayer.BLOCK, feet) > 0) {
            return false;
        }
        BlockPos ground = feet.below();
        if (!level.getFluidState(feet).isEmpty() || !level.getBlockState(ground).getFluidState().isEmpty()) {
            return false;
        }
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
            return false;
        }
        for (int offset = 0; offset < airBlocks; offset++) {
            BlockState state = level.getBlockState(feet.above(offset));
            if (!state.getFluidState().isEmpty()) {
                return false;
            }
            if (!state.isAir() && !state.is(BlockTags.REPLACEABLE)) {
                return false;
            }
        }
        return true;
    }
}
