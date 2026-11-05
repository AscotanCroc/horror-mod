package com.horrormod.mixin;

import com.horrormod.world.SpawnPlains;
import com.horrormod.world.VoidChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public class SpawnPlainsLavaMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void horror$skipHeavyFeatures(
            WorldGenLevel level,
            ChunkGenerator generator,
            RandomSource random,
            BlockPos origin,
            CallbackInfoReturnable<Boolean> callback
    ) {
        Feature feature = ((PlacedFeature) (Object) this).feature().value();
        if (!(feature instanceof LakeFeature) && !(feature instanceof MonsterRoomFeature)) {
            return;
        }
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        ChunkPos chunk = ChunkPos.containing(origin);
        ServerLevel serverLevel = ((ServerLevelAccessor) region).getLevel();
        if (feature instanceof MonsterRoomFeature && VoidChunks.isRemoved(serverLevel, chunk.x(), chunk.z())) {
            callback.setReturnValue(false);
            return;
        }
        if (feature instanceof LakeFeature lake
                && lake.fluid().value() instanceof SimpleStateProvider provider
                && provider.state().is(Blocks.LAVA)
                && serverLevel.dimension().equals(Level.OVERWORLD)) {
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
            if (origin.getY() < floor || SpawnPlains.containsChunk(chunk)) {
                callback.setReturnValue(false);
            }
        }
    }
}