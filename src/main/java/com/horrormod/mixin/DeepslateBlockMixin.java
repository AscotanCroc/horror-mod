package com.horrormod.mixin;

import com.horrormod.world.DeepslateStone;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenRegion.class)
public class DeepslateBlockMixin {
    @ModifyVariable(method = "setBlock", at = @At("HEAD"), argsOnly = true)
    private BlockState horror$stoneInsteadOfDeepslate(BlockState state) {
        return DeepslateStone.toStone(state);
    }
}
