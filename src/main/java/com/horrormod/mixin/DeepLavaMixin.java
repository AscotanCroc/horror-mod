package com.horrormod.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseBasedChunkGenerator.class)
public class DeepLavaMixin {
    @Redirect(
            method = "createFluidPicker",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/Blocks;LAVA:Lnet/minecraft/world/level/block/Block;"
            )
    )
    private static Block horror$noDeepLava() {
        return Blocks.AIR;
    }
}
