package com.horrormod.client.mixin;

import com.horrormod.client.entity.ClientFalsePlayer;
import com.horrormod.client.entity.ClientNullClone;
import com.horrormod.client.render.FalsePlayerRenderers;
import com.horrormod.client.render.NullCloneRenderers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class NullCloneRendererMixin {
    @Inject(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
    private void horror$cloneRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?, ?>> callback) {
        if (entity instanceof ClientNullClone clone && NullCloneRenderers.renderers != null) {
            callback.setReturnValue(renderer(NullCloneRenderers.renderers, clone.getSkin().model()));
            return;
        }
        if (entity instanceof ClientFalsePlayer clone && FalsePlayerRenderers.renderers != null) {
            callback.setReturnValue(renderer(FalsePlayerRenderers.renderers, clone.getSkin().model()));
        }
    }

    private static <T extends net.minecraft.world.entity.Avatar & net.minecraft.client.entity.ClientAvatarEntity> AvatarRenderer<T> renderer(
            java.util.Map<PlayerModelType, AvatarRenderer<T>> renderers,
            PlayerModelType model
    ) {
        AvatarRenderer<T> renderer = renderers.get(model);
        if (renderer == null) {
            renderer = renderers.get(PlayerModelType.WIDE);
        }
        return renderer;
    }
}
