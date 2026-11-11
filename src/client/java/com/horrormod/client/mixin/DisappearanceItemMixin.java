package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.render.GlitchFrame;
import com.horrormod.entity.HorrorEntities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inventory icon for disappearance. One 32x32 frame of the shot's strip, twice a tick.
 */
@Mixin(GuiGraphicsExtractor.class)
public class DisappearanceItemMixin {
    private static final Identifier TEXTURE = HorrorMod.id("textures/entity/glitch_square.png");
    private static final int FRAME = 32;
    private static final int STRIP = FRAME * GlitchFrame.COUNT;

    @Inject(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void horror$glitchIcon(
            LivingEntity entity,
            Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callback
    ) {
        if (!stack.is(HorrorEntities.DISSAPEARANCE)) {
            return;
        }
        callback.cancel();
        ((GuiGraphicsExtractor) (Object) this).blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x,
                y,
                0.0F,
                GlitchFrame.index() * FRAME,
                16,
                16,
                FRAME,
                FRAME,
                FRAME,
                STRIP,
                0xFFFFFFFF
        );
    }
}
