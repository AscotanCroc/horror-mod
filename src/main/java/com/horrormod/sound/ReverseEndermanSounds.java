package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ReverseEndermanSounds {
    public static final SoundEvent AMBIENT = register("reverse_enderman_ambient");
    public static final SoundEvent SCREAM = register("reverse_enderman_scream");
    public static final SoundEvent HURT = register("reverse_enderman_hurt");
    public static final SoundEvent DEATH = register("reverse_enderman_death");
    public static final SoundEvent STARE = register("reverse_enderman_stare");
    public static final SoundEvent TELEPORT = register("reverse_enderman_teleport");

    private ReverseEndermanSounds() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered reverse enderman sounds");
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
