package com.horrormod.mixin;

import com.horrormod.world.SpawnPlains;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class SpawnPlainsBiomeMixin {
    @Inject(method = {"createResolver", "createResolverForChunk"}, at = @At("RETURN"), cancellable = true)
    private void horror$forceSpawnPlains(CallbackInfoReturnable<BiomeResolver> callback) {
        MultiNoiseBiomeSource source = (MultiNoiseBiomeSource) (Object) this;
        if (!source.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
            return;
        }
        Holder<Biome> plains = SpawnPlains.plains(source);
        if (plains == null) {
            return;
        }
        BiomeResolver delegate = callback.getReturnValue();
        callback.setReturnValue((x, y, z) -> SpawnPlains.containsQuart(x, z) ? plains : delegate.getNoiseBiome(x, y, z));
    }
}
