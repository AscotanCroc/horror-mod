package com.horrormod.client.render;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Walk cycle from the Blockbench model. One second, looping, linear.
 * Diagonal feet swap, and the nose and tip pitch with them.
 */
public final class WatdatmeanAnimation {
    private static final AnimationChannel NOSE = pitch(0.0F, 22.5F, 0.25F, 2.5F, 0.5F, -27.5F, 0.75F, 2.5F, 1.0F, 22.5F);
    private static final AnimationChannel TIP = pitch(0.0F, 77.5F, 0.25F, 10.0F, 0.5F, -20.0F, 0.75F, 10.0F, 1.0F, 77.5F);
    private static final AnimationChannel LEFT_DIAGONAL = pitch(0.0F, -57.5F, 0.25F, 0.0F, 0.5F, 55.0F, 0.75F, 0.0F, 1.0F, -57.5F);
    private static final AnimationChannel RIGHT_DIAGONAL = pitch(0.0F, 55.0F, 0.25F, 0.0F, 0.5F, -57.5F, 0.75F, 0.0F, 1.0F, 55.0F);

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("nose", NOSE)
            .addAnimation("tip", TIP)
            .addAnimation("bottom left", LEFT_DIAGONAL)
            .addAnimation("top right", LEFT_DIAGONAL)
            .addAnimation("top left", RIGHT_DIAGONAL)
            .addAnimation("bottom right", RIGHT_DIAGONAL)
            .build();

    private WatdatmeanAnimation() {
    }

    private static AnimationChannel pitch(float... samples) {
        Keyframe[] frames = new Keyframe[samples.length / 2];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Keyframe(
                    samples[i * 2],
                    KeyframeAnimations.degreeVec(samples[i * 2 + 1], 0.0F, 0.0F),
                    AnimationChannel.Interpolations.LINEAR
            );
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, frames);
    }
}
