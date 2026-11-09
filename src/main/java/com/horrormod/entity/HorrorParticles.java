package com.horrormod.entity;

import com.horrormod.HorrorMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public final class HorrorParticles {
    public static final SimpleParticleType GLITCH_SQUARE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            HorrorMod.id("glitch_square"),
            FabricParticleTypes.simple()
    );
    public static final SimpleParticleType BLACK_SQUARE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            HorrorMod.id("black_square"),
            FabricParticleTypes.simple()
    );
    public static final SimpleParticleType BLACK_SMOKE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            HorrorMod.id("black_smoke"),
            FabricParticleTypes.simple()
    );

    private HorrorParticles() {
    }

    public static void initialize() {
        HorrorMod.LOGGER.info("Registered glitch square particle {}", GLITCH_SQUARE);
    }
}
