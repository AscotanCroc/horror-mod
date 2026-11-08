package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ReverseSkeletonSounds {
    public static final SoundEvent AMBIENT = register("reverse_skeleton_ambient");
    public static final SoundEvent HURT = register("reverse_skeleton_hurt");
    public static final SoundEvent DEATH = register("reverse_skeleton_death");
    public static final SoundEvent STEP = register("reverse_skeleton_step");
    public static final SoundEvent SHOOT = register("reverse_skeleton_shoot");

    private ReverseSkeletonSounds() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered reverse skeleton sounds");
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
