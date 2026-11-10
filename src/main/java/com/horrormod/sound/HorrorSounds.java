package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class HorrorSounds {
    public static final SoundEvent AMBIENCE = register("ambience");
    public static final SoundEvent NIGHT = register("night");
    public static final SoundEvent GLITCH_TRANSITION = register("glitch_transition");
    public static final SoundEvent WHISPER = register("whisper");
    public static final SoundEvent VEIL = register("veil");
    public static final SoundEvent VEIL_SCREAM = register("veil_scream");

    private HorrorSounds() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info(
                "Registered ambience {}, night {}, glitch transition {}, whisper {}, and veil {}",
                AMBIENCE.location(),
                NIGHT.location(),
                GLITCH_TRANSITION.location(),
                WHISPER.location(),
                VEIL.location()
        );
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
