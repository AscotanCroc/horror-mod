package com.horrormod.client.mixin;

import com.horrormod.entity.FalsePlayer;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The distant player copy fades from full at {@link FalsePlayer#FADE_START} blocks to nothing at 15.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class FalsePlayerFadeMixin {
    private static final Map<LivingEntityRenderState, Float> OPACITY = new IdentityHashMap<>();

    @Shadow
    public abstract Identifier getTextureLocation(LivingEntityRenderState state);

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void horror$noteFade(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo callback) {
        if (!(entity instanceof FalsePlayer)) {
            OPACITY.remove(state);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float opacity = 1.0F;
        if (minecraft.player != null) {
            double distance = entity.distanceTo(minecraft.player);
            if (distance <= FalsePlayer.REMOVE_DISTANCE) {
                opacity = 0.0F;
            } else if (distance < FalsePlayer.FADE_START) {
                opacity = (float) ((distance - FalsePlayer.REMOVE_DISTANCE) / (FalsePlayer.FADE_START - FalsePlayer.REMOVE_DISTANCE));
            }
        }
        OPACITY.put(state, opacity);
    }

    @Inject(method = "getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I", at = @At("RETURN"), cancellable = true)
    private void horror$fadeTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> callback) {
        Float opacity = OPACITY.get(state);
        if (opacity == null || opacity >= 0.99F) {
            return;
        }
        int alpha = Mth.clamp((int) (opacity * 255.0F), 0, 255);
        callback.setReturnValue((callback.getReturnValue() & 0x00FFFFFF) | (alpha << 24));
    }

    @Inject(
            method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void horror$fadeType(
            LivingEntityRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing,
            CallbackInfoReturnable<RenderType> callback
    ) {
        Float opacity = OPACITY.get(state);
        if (opacity == null || opacity >= 0.99F || callback.getReturnValue() == null) {
            return;
        }
        Identifier texture = this.getTextureLocation(state);
        callback.setReturnValue(RenderTypes.entityTranslucent(texture));
    }
}
