package com.horrormod.sound;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ReverseSpiderSounds {
    public static final SoundEvent AMBIENT = register("reverse_spider_ambient");
    public static final SoundEvent HURT = register("reverse_spider_hurt");
    public static final SoundEvent DEATH = register("reverse_spider_death");
    public static final SoundEvent STEP = register("reverse_spider_step");
    public static final SoundEvent KILL = register("reverse_spider_kill");
    public static final SoundEvent COBWEB_BREAK = register("black_cobweb_break");
    public static final SoundEvent COBWEB_STEP = register("black_cobweb_step");

    private ReverseSpiderSounds() {
    }

    public static boolean glitches(Identifier id) {
        return HorrorMod.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("reverse_spider_") && !id.equals(KILL.location());
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered reverse spider sounds");
    }

    private static SoundEvent register(String path) {
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                HorrorMod.id(path),
                SoundEvent.createVariableRangeEvent(HorrorMod.id(path))
        );
    }
}
