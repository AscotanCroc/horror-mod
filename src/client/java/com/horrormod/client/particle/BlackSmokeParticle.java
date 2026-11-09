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
 * Campfire smoke, stretched out, almost clear, and black.
 */
public class BlackSmokeParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    private BlackSmokeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.lifetime = 50 + level.getRandom().nextInt(30);
        this.gravity = -0.004F;
        this.hasPhysics = false;
        this.quadSize = 2.8F + level.getRandom().nextFloat() * 2.4F;
        this.alpha = 0.16F;
        this.setColor(0.02F, 0.02F, 0.02F);
        this.xd = (level.getRandom().nextFloat() - 0.5F) * 0.02F;
        this.yd = 0.015F + level.getRandom().nextFloat() * 0.02F;
        this.zd = (level.getRandom().nextFloat() - 0.5F) * 0.02F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = Math.max(0.02F, 0.16F * (1.0F - (float) this.age / this.lifetime));
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
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
            return new BlackSmokeParticle(level, x, y, z, this.sprites);
        }
    }
}
