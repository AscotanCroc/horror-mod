package com.horrormod.sound;

import java.lang.StackWalker.StackFrame;

/**
 * Every real footstep is twice as loud. Callers named {@code playStepSound} are the only ones scaled,
 * so hurt and ambient sounds stay put and a later volume change still multiplies this result.
 */
public final class StepVolume {
    public static final float SCALE = 2.0F;
    private static final StackWalker WALKER = StackWalker.getInstance();

    private StepVolume() {
    }

    public static boolean fromStep() {
        return WALKER.walk(frames -> frames.limit(16).map(StackFrame::getMethodName).anyMatch(StepVolume::isStep));
    }

    private static boolean isStep(String name) {
        return name.equals("playStepSound")
                || name.equals("playMuffledStepSound")
                || name.equals("playAmethystStepSound")
                || name.equals("playCombinationStepSounds");
    }
}
