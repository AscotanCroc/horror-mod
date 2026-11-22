package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import java.lang.reflect.Field;
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
    private static Field animationInfo;
    private static boolean lookupFailed;

    @Accessor("frame")
    abstract int horror$frame();

    @Accessor("frame")
    abstract void horror$frame(int frame);

    @Accessor("subFrame")
    abstract void horror$subFrame(int subFrame);

    @Accessor("isDirty")
    abstract void horror$dirty(boolean dirty);

    @Inject(method = "tick", at = @At("RETURN"))
    private void horror$matchShot(CallbackInfo callback) {
        Object info = horror$info();
        if (!(info instanceof DisappearanceTextureAccessor texture)) {
            return;
        }
        SpriteContents contents = texture.horror$contents();
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

    /**
     * The animation holder is a private inner class, so the accessor cannot name it.
     * The field is read once and kept.
     */
    private Object horror$info() {
        if (lookupFailed) {
            return null;
        }
        try {
            Field field = animationInfo;
            if (field == null) {
                field = getClass().getDeclaredField("animationInfo");
                field.setAccessible(true);
                animationInfo = field;
            }
            return field.get(this);
        } catch (Exception exception) {
            lookupFailed = true;
            return null;
        }
    }
}
