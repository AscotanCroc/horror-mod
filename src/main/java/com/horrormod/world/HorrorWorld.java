package com.horrormod.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;

public final class HorrorWorld {
    private HorrorWorld() {
    }

    public static void initialize() {
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
        });
        ServerChunkEvents.CHUNK_LOAD.register(VoidChunks::clear);
    }

    private static void warmGeneration(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        ChunkPos spawn = SpawnPlains.spawnChunk(overworld);
        ObeliskPlacement.chunk(overworld.getSeed(), spawn);
    }
}
