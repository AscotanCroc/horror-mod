package com.horrormod.client.mixin;

import com.horrormod.HorrorMod;
import com.horrormod.client.render.VeilItemPipeline;
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

@Mixin(GuiGraphicsExtractor.class)
public class VeilItemMixin {
    private static final Identifier TEXTURE = HorrorMod.id("textures/item/veil.png");
    private static final int TEXTURE_SIZE = 980;
    private static final int COPIES = 9;

    @Inject(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void horror$veilCopies(
            LivingEntity entity,
            Level level,
            ItemStack stack,
            int x,
            int y,
            int seed,
            CallbackInfo callback
    ) {
        if (!stack.is(HorrorEntities.VEIL)) {
            return;
        }
        callback.cancel();
        float seconds = (System.nanoTime() % 1_000_000_000_000L) / 1_000_000_000.0F;
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        for (int copy = 0; copy < COPIES; copy++) {
            float speed = 0.85F + (copy % 3) * 0.4F;
            float angle = seconds * speed + copy * 0.85F;
            float radius = 5.0F + (copy % 4) * 1.8F;
            float wobble = (float) Math.sin(seconds * 1.6F + copy * 1.7F);
            int dx = Math.round((float) Math.cos(angle) * (radius + wobble));
            int dy = Math.round((float) Math.sin(angle * 0.75F + copy) * radius * 0.72F);
            int size = 7 + (copy % 3) * 3;
            float hue = (copy / (float) COPIES + seconds * 0.13F) % 1.0F;
            int alpha = 48 + (copy * 19) % 100;
            int color = VeilItemPipeline.color(hue, alpha);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x + 8 - size / 2 + dx,
                    y + 8 - size / 2 + dy,
                    0.0F,
                    0.0F,
                    size,
                    size,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE,
                    color
            );
        }
        graphics.blit(
                VeilItemPipeline.PIPELINE,
                TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                16,
                16,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                VeilItemPipeline.color()
        );
    }
}
