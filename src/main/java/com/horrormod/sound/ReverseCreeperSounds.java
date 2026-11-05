package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ReverseCreeperSounds {
    public static final SoundEvent PRIMED = register("reverse_creeper_primed");
    public static final SoundEvent EXPLODE = register("reverse_creeper_explode");
    public static final SoundEvent HURT = register("reverse_creeper_hurt");
    public static final SoundEvent DEATH = register("reverse_creeper_death");

    private ReverseCreeperSounds() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered reverse creeper sounds");
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
