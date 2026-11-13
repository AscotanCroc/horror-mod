package com.horrormod.mixin;

import com.horrormod.entity.HorrorEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reverse monsters are not part of the hostile pack roll or the hostile cap.
 * {@link com.horrormod.entity.ReverseZombieNights} places them into the surface and underground slots.
 */
@Mixin(NaturalSpawner.class)
public class ReverseMobCapMixin {
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
        if (category != MobCategory.MONSTER) {
            return list;
        }
        return withoutReverse(list);
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
