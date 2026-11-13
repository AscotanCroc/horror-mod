package com.horrormod.client.render;

import com.horrormod.client.entity.ClientFalsePlayer;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import com.horrormod.entity.FalsePlayer;

public final class FalsePlayerRenderers {
    public static Map<PlayerModelType, AvatarRenderer<ClientFalsePlayer>> renderers;

    private FalsePlayerRenderers() {
    }

    public static EntityRenderer<FalsePlayer, EntityRenderState> create(EntityRendererProvider.Context context) {
        renderers = EntityRenderers.createAvatarRenderers(context);
        return new EntityRenderer<>(context) {
            @Override
            public EntityRenderState createRenderState() {
                return new EntityRenderState();
            }
        };
    }
}
