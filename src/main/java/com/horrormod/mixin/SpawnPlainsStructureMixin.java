package com.horrormod.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.world.ObeliskPlacement;
import com.horrormod.world.SpawnPlains;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public class SpawnPlainsStructureMixin {
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void horror$onlyObeliskInSpawnPlains(
            Holder<Structure> structure,
            ResourceKey<Level> level,
            RegistryAccess registries,
            ChunkGenerator generator,
            BiomeSource biomeSource,
            Climate.Sampler sampler,
            RandomState randomState,
            StructureTemplateManager templates,
            long seed,
            ChunkPos chunkPos,
            int references,
            LevelHeightAccessor heightAccessor,
            Predicate<Holder<Biome>> validBiome,
            CallbackInfoReturnable<StructureStart> callback
    ) {
        Identifier id = structure.unwrapKey().map(ResourceKey::identifier).orElse(null);
        if (id != null && HorrorMod.MOD_ID.equals(id.getNamespace()) && id.getPath().equals("obelisk")) {
            if (level != Level.OVERWORLD
                    || !chunkPos.equals(ObeliskPlacement.chunk(seed, SpawnPlains.spawnChunk(seed, generator, randomState)))) {
                callback.setReturnValue(StructureStart.INVALID_START);
            }
            return;
        }
        if (id != null && "minecraft".equals(id.getNamespace()) && id.getPath().equals("trial_chambers")) {
            callback.setReturnValue(StructureStart.INVALID_START);
            return;
        }
        if (level != Level.OVERWORLD || !SpawnPlains.containsChunk(chunkPos)) {
            return;
        }
        if (id != null && HorrorMod.MOD_ID.equals(id.getNamespace())) {
            return;
        }
        callback.setReturnValue(StructureStart.INVALID_START);
    }
}
