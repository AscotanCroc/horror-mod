package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class PlainsLife {
    private PlainsLife() {
    }

    public static void initialize() {
        BiomeModifications.create(HorrorMod.id("plains_clearing")).add(
                ModificationPhase.REMOVALS,
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS),
                context -> {
                    var generation = context.getGenerationSettings();
                    generation.removeFeature(placed("patch_tall_grass"));
                    generation.removeFeature(placed("patch_tall_grass_2"));
                    generation.removeFeature(placed("patch_grass_plain"));
                    generation.removeFeature(placed("patch_bush"));
                    generation.removeFeature(placed("patch_firefly_bush_near_water"));
                    generation.removeFeature(placed("patch_pumpkin"));
                    generation.removeFeature(placed("patch_sugar_cane"));
                    generation.removeFeature(placed("flower_plains"));
                    generation.removeFeature(placed("patch_sunflower"));
                    context.getMobSpawnSettings().clearSpawns(MobCategory.CREATURE);
                }
        );
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, HorrorMod.id("rare_short_grass"))
        );
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.withDefaultNamespace(path));
    }
}
