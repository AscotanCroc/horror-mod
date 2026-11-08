package com.horrormod.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * A square from the projectile texture. Each one flips through the same frames.
 */
public class GlitchSquareParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    private GlitchSquareParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, sprites.first());
        this.sprites = sprites;
        this.lifetime = 10 + level.getRandom().nextInt(8);
        this.gravity = 0.0F;
        this.quadSize = 0.12F + level.getRandom().nextFloat() * 0.28F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
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
        private final SpriteSet sprites;

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
            return new GlitchSquareParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
