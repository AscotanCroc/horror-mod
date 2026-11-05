package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ReverseZombieSounds {
    public static final SoundEvent AMBIENT = register("reverse_zombie_ambient");
    public static final SoundEvent HURT = register("reverse_zombie_hurt");
    public static final SoundEvent DEATH = register("reverse_zombie_death");
    public static final SoundEvent STEP = register("reverse_zombie_step");
    public static final SoundEvent ATTACK_DOOR = register("reverse_zombie_attack_door");
    public static final SoundEvent BREAK_DOOR = register("reverse_zombie_break_door");
    public static final SoundEvent DESTROY_EGG = register("reverse_zombie_destroy_egg");

    private ReverseZombieSounds() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered reverse zombie sounds");
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
