package com.horrormod.mixin;

import com.horrormod.world.VoidChunks;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class VoidTerrainMixin {
    @Inject(method = "buildTerrain", at = @At("HEAD"), cancellable = true)
    private void horror$skipVoidTerrain(
            ChunkAccess chunk,
            Blender blender,
            RandomState randomState,
            StructureManager structures,
            BiomeManager biomes,
            WorldGenRegion region,
            Set<Holder<Biome>> possibleBiomes,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback
    ) {
        if (removed(region, chunk)) {
            callback.setReturnValue(CompletableFuture.completedFuture(chunk));
        }
    }

    private static boolean removed(WorldGenRegion region, ChunkAccess chunk) {
        ServerLevel level = region.getLevel();
        ChunkPos pos = chunk.getPos();
        return VoidChunks.isRemoved(level, pos.x(), pos.z());
    }
}

@Mixin(ChunkGenerator.class)
abstract class VoidDecorationMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
    private void horror$skipVoidDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures, CallbackInfo callback) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        if (VoidChunks.isRemoved(region.getLevel(), pos.x(), pos.z())) {
            callback.cancel();
        }
    }
}
