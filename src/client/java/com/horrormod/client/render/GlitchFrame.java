package com.horrormod.client.render;

/**
 * Which frame of the glitch strip to show.
 * The shot advances twice a tick, which is forty frames a second.
 */
public final class GlitchFrame {
    public static final int COUNT = 32;
    private static final long NANOS_PER_FRAME = 25_000_000L;

    private GlitchFrame() {
    }

    public static int index() {
        return (int) Math.floorMod(System.nanoTime() / NANOS_PER_FRAME, COUNT);
    }
}
