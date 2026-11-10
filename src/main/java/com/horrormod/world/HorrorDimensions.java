package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The veil is rows of bridges running one way through the void.
 */
public final class HorrorDimensions {
    public static final ResourceKey<Level> VEIL = ResourceKey.create(Registries.DIMENSION, HorrorMod.id("veil"));

    private HorrorDimensions() {
    }
}
