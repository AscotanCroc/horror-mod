package com.horrormod.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverworldBiomeBuilder.class)
public class CaveBiomeMixin {
    @Inject(method = "addUndergroundBiomes", at = @At("HEAD"), cancellable = true)
    private void horror$noSpecialCaves(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes,
            CallbackInfo callback
    ) {
        callback.cancel();
    }
}
