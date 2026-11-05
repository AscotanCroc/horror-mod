package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Placeholder dimensions. The veil is an empty flat room until the real space exists.
 */
public final class HorrorDimensions {
    public static final ResourceKey<Level> VEIL = ResourceKey.create(Registries.DIMENSION, HorrorMod.id("veil"));

    private HorrorDimensions() {
    }
}
