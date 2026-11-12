package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The shot changes picture twice a tick. The item sprite is told that frame
 * after the ordinary animation step, so the icon, the hand, and the ground all match.
 */
@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimationState")
public abstract class DisappearanceFrameMixin {
    private static final long NANOS_PER_FRAME = 25_000_000L;

    @Accessor("frame")
    abstract int horror$frame();

    @Accessor("frame")
    abstract void horror$frame(int frame);

    @Accessor("subFrame")
    abstract void horror$subFrame(int subFrame);

    @Accessor("isDirty")
    abstract void horror$dirty(boolean dirty);

    @Accessor("animationInfo")
    abstract Object horror$info();

    @Inject(method = "tick", at = @At("RETURN"))
    private void horror$matchShot(CallbackInfo callback) {
        SpriteContents contents = ((DisappearanceTextureAccessor) horror$info()).horror$contents();
        if (!HorrorMod.id("item/disappearance").equals(contents.name())) {
            return;
        }
        int count = contents.getUniqueFrames().size();
        if (count <= 1) {
            return;
        }
        long index = System.nanoTime() / NANOS_PER_FRAME;
        horror$frame((int) Math.floorMod(index, count));
        horror$subFrame(0);
        horror$dirty(true);
    }
}
