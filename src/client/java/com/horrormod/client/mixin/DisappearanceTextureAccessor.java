package com.horrormod.client.mixin;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimatedTexture")
public interface DisappearanceTextureAccessor {
    @Accessor("this$0")
    SpriteContents horror$contents();
}
