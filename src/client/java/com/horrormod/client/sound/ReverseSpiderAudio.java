package com.horrormod.client.sound;

import com.horrormod.sound.ReverseSpiderSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.RandomSource;

/**
 * Stops some of the spider's stacked sounds early so they cut off mid-playback.
 */
public final class ReverseSpiderAudio {
    private static final List<Cut> CUTS = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private ReverseSpiderAudio() {
    }

    public static void maybeCut(SoundEngine engine, SoundInstance sound) {
        if (!ReverseSpiderSounds.glitches(sound.getIdentifier()) || RANDOM.nextFloat() > 0.9F) {
            return;
        }
        CUTS.add(new Cut(engine, sound, RANDOM.nextInt(4)));
    }

    public static void tick() {
        Iterator<Cut> cuts = CUTS.iterator();
        while (cuts.hasNext()) {
            Cut cut = cuts.next();
            if (--cut.ticks > 0) {
                continue;
            }
            cut.engine.stop(cut.sound);
            cuts.remove();
        }
    }

    private static final class Cut {
        private final SoundEngine engine;
        private final SoundInstance sound;
        private int ticks;

        private Cut(SoundEngine engine, SoundInstance sound, int ticks) {
            this.engine = engine;
            this.sound = sound;
            this.ticks = ticks;
        }
    }
}
