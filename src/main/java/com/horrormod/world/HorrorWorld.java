package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.ChunkPos;

public final class HorrorWorld {
    /** Overworld sunrise, held so the veil sky does not advance into day. */
    private static final long DAWN_TICK = 23000L;

    private HorrorWorld() {
    }

    public static void initialize() {
        BridgeChunkGenerator.initialize();
        PlainsLife.initialize();
        PlainsDrops.initialize();
        HorrorStructures.initialize();
        VeilPassage.initialize();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.setDifficulty(Difficulty.EASY, true);
            server.setDifficultyLocked(true);
            server.getWorldData().setDifficulty(Difficulty.EASY);
            server.getWorldData().setDifficultyLocked(true);
            warmGeneration(server);
            holdDawn(server);
        });
        ServerChunkEvents.CHUNK_LOAD.register(VoidChunks::clear);
        ServerChunkEvents.CHUNK_LOAD.register(BridgeChunkGenerator::ensureFloor);
    }

    private static void warmGeneration(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        ChunkPos spawn = SpawnPlains.spawnChunk(overworld);
        ObeliskPlacement.chunk(overworld.getSeed(), spawn);
    }

    private static void holdDawn(MinecraftServer server) {
        ResourceKey<WorldClock> key = ResourceKey.create(Registries.WORLD_CLOCK, HorrorMod.id("dawn"));
        Holder<WorldClock> clock = server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(key);
        ServerClockManager clocks = server.clockManager();
        clocks.setTotalTicks(clock, DAWN_TICK);
        clocks.setPaused(clock, true);
    }
}
