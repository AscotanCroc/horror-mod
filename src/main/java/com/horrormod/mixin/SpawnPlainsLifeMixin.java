package com.horrormod.mixin;

import com.horrormod.world.SpawnPlains;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
abstract class SpawnPlainsLifeMixin {
    @Inject(method = "getMobsAt", at = @At("HEAD"), cancellable = true)
    private void horror$noAnimalsInSpawnPlains(
            Level level,
            StructureManager structures,
            MobCategory category,
            BlockPos pos,
            CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> callback
    ) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (category == MobCategory.CREATURE && SpawnPlains.containsChunk(ChunkPos.containing(pos))) {
            callback.setReturnValue(WeightedList.of());
            return;
        }
        if (isPassiveAquatic(category) && plainsColumn(level, pos)) {
            callback.setReturnValue(WeightedList.of());
        }
    }

    private static boolean isPassiveAquatic(MobCategory category) {
        return category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.AXOLOTLS;
    }

    private static boolean plainsColumn(Level level, BlockPos pos) {
        if (isPlains(level.getBiome(pos))) {
            return true;
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos surfacePos = new BlockPos(pos.getX(), Math.max(level.getMinY(), surface - 1), pos.getZ());
        return isPlains(level.getBiome(surfacePos));
    }

    private static boolean isPlains(Holder<Biome> biome) {
        return biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS);
    }
}

@Mixin(NoiseBasedChunkGenerator.class)
abstract class SpawnPlainsMobGenMixin {
    @Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
    private void horror$noAnimalsAtGeneration(WorldGenRegion region, CallbackInfo callback) {
        if (((ServerLevelAccessor) region).getLevel().dimension() == Level.OVERWORLD
                && SpawnPlains.containsChunk(region.getCenter())) {
            callback.cancel();
        }
    }
}
