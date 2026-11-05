package com.horrormod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpringFeature.class)
public class LavaSpringMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void horror$noUndergroundLavaSprings(
            WorldGenLevel level,
            ChunkGenerator generator,
            RandomSource random,
            BlockPos origin,
            CallbackInfoReturnable<Boolean> callback
    ) {
        SpringFeature spring = (SpringFeature) (Object) this;
        if (!spring.state().is(Fluids.LAVA) && !spring.state().is(Fluids.FLOWING_LAVA)) {
            return;
        }
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        ServerLevel serverLevel = ((ServerLevelAccessor) region).getLevel();
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        if (origin.getY() < floor) {
            callback.setReturnValue(false);
        }
    }
}
