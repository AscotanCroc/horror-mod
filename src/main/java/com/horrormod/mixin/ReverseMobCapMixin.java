package com.horrormod.mixin;

import com.horrormod.entity.HorrorEntities;
import com.horrormod.entity.ReverseMonsterSpawns;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reverse monsters keep the hostile spawn roll, but they use the surface and dark slots
 * instead of the hostile mob cap. A full cap does not block them, and they do not fill it.
 */
@Mixin(NaturalSpawner.class)
public class ReverseMobCapMixin {
    private static boolean rolling;

    @Redirect(
            method = "createState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;getCategory()Lnet/minecraft/world/entity/MobCategory;"
            )
    )
    private static MobCategory horror$uncountReverse(EntityType<?> type) {
        if (HorrorEntities.isReverseType(type)) {
            return MobCategory.MISC;
        }
        return type.getCategory();
    }

    @Redirect(
            method = "mobsAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getMobsAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/util/random/WeightedList;"
            )
    )
    private static WeightedList<MobSpawnSettings.SpawnerData> horror$hostileList(
            ChunkGenerator generator,
            Level level,
            StructureManager structures,
            MobCategory category,
            BlockPos pos
    ) {
        WeightedList<MobSpawnSettings.SpawnerData> list = generator.getMobsAt(level, structures, category, pos);
        if (rolling || category != MobCategory.MONSTER) {
            return list;
        }
        return withoutReverse(list);
    }

    @Inject(method = "spawnForChunk", at = @At("RETURN"))
    private static void horror$rollReverse(
            ServerLevel level,
            LevelChunk chunk,
            NaturalSpawner.SpawnState state,
            List<MobCategory> categories,
            CallbackInfo callback
    ) {
        if (level.dimension() != Level.OVERWORLD || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (!level.getGameRules().get(GameRules.SPAWN_MOBS) || !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) {
            return;
        }
        if (ReverseMonsterSpawns.surfaceOccupied(level) && ReverseMonsterSpawns.darkOccupied(level)) {
            return;
        }
        rolling = true;
        try {
            NaturalSpawner.spawnCategoryForChunk(
                    MobCategory.MONSTER,
                    level,
                    chunk,
                    (type, world, pos, access) -> HorrorEntities.isReverseType(type),
                    (mob, access) -> { }
            );
        } finally {
            rolling = false;
        }
    }

    private static WeightedList<MobSpawnSettings.SpawnerData> withoutReverse(
            WeightedList<MobSpawnSettings.SpawnerData> list
    ) {
        List<Weighted<MobSpawnSettings.SpawnerData>> kept = new ArrayList<>();
        boolean removed = false;
        for (Weighted<MobSpawnSettings.SpawnerData> entry : list.unwrap()) {
            if (HorrorEntities.isReverseType(entry.value().type())) {
                removed = true;
                continue;
            }
            kept.add(entry);
        }
        if (!removed) {
            return list;
        }
        if (kept.isEmpty()) {
            return WeightedList.of();
        }
        return WeightedList.of(kept);
    }
}
