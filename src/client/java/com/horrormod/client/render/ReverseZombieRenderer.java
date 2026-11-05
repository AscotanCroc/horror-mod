package com.horrormod.client.render;

import com.horrormod.HorrorMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class ReverseZombieRenderer extends ZombieRenderer {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/reverse_zombie.png");
    private static final int PITCH_BLACK = 0xFF000000;

    public ReverseZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ReverseZombieRenderState();
    }

    @Override
    public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof ReverseZombieRenderState reverse) {
            reverse.entityId = entity.getId();
        }
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getModelTint(ZombieRenderState state) {
        if (state instanceof ReverseZombieRenderState reverse && ReverseZombieFlash.isBlack(reverse.entityId)) {
            return PITCH_BLACK;
        }
        return super.getModelTint(state);
    }
}
