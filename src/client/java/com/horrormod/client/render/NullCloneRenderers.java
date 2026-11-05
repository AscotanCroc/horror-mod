package com.horrormod.client.render;

import com.horrormod.client.entity.ClientNullClone;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import com.horrormod.entity.NullClone;

public final class NullCloneRenderers {
    public static Map<PlayerModelType, AvatarRenderer<ClientNullClone>> renderers;

    private NullCloneRenderers() {
    }

    public static EntityRenderer<NullClone, EntityRenderState> create(EntityRendererProvider.Context context) {
        renderers = EntityRenderers.createAvatarRenderers(context);
        return new EntityRenderer<>(context) {
            @Override
            public EntityRenderState createRenderState() {
                return new EntityRenderState();
            }
        };
    }
}
