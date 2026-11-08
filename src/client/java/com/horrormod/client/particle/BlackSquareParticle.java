package com.horrormod.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * A solid black square. Light does not change it.
 */
public class BlackSquareParticle extends SingleQuadParticle {
    private BlackSquareParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FabricSpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, sprites.first());
        this.lifetime = 14 + level.getRandom().nextInt(12);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.18F + level.getRandom().nextFloat() * 0.55F;
        this.setSprite(sprites.first());
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final FabricSpriteSet sprites;

        public Provider(FabricSpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz,
                RandomSource random
        ) {
            return new BlackSquareParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
