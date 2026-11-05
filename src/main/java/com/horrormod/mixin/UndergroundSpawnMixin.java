package com.horrormod.mixin;

import com.horrormod.entity.HorrorEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class UndergroundSpawnMixin {
    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
    private static void horror$halfUnderground(
            ServerLevel level,
            MobCategory category,
            StructureManager structures,
            ChunkGenerator generator,
            MobSpawnSettings.SpawnerData data,
            BlockPos.MutableBlockPos pos,
            double distance,
            CallbackInfoReturnable<Boolean> callback
    ) {
        EntityType<?> type = data.type();
        if (type == HorrorEntities.REVERSE_ZOMBIE || type == HorrorEntities.REVERSE_ENDERMAN || type == HorrorEntities.REVERSE_CREEPER) {
            return;
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() < surface && level.getRandom().nextBoolean()) {
            callback.setReturnValue(false);
        }
    }

    @Redirect(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/valueproviders/IntProvider;sample(Lnet/minecraft/util/RandomSource;)I"
            )
    )
    private static int horror$oneAtATime(IntProvider provider, RandomSource random) {
        return 1;
    }

    @Redirect(
            method = "spawnMobsForChunkGeneration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/valueproviders/IntProvider;sample(Lnet/minecraft/util/RandomSource;)I"
            )
    )
    private static int horror$oneOnGeneration(IntProvider provider, RandomSource random) {
        return 1;
    }
}
