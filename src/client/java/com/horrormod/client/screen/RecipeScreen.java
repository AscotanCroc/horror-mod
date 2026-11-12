package com.horrormod.client.screen;

import com.horrormod.entity.HorrorEntities;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The lectern recipes: the veil, and the item it is made from, each drifting with a fading rainbow trail.
 */
public class RecipeScreen extends Screen {
    private static final Identifier CRAFTING_TABLE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    /** Crop of the crafting texture that contains the grid, arrow, and result slot. */
    private static final int CROP_U = 20;
    private static final int CROP_V = 10;
    private static final int IMAGE_WIDTH = 140;
    private static final int IMAGE_HEIGHT = 66;
    private static final int TRAIL = 8;

    private final ItemStack[] veilGrid = grid(
            new ItemStack(Items.BONE),
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(HorrorEntities.FIVE)
    );
    private final ItemStack veilResult = new ItemStack(HorrorEntities.VEIL);
    private final ItemStack[] fiveGrid = grid(
            new ItemStack(HorrorEntities.GLITCH),
            new ItemStack(HorrorEntities.NULL_POINTER),
            new ItemStack(HorrorEntities.SAVIOR),
            new ItemStack(HorrorEntities.DISAPPEARANCE),
            new ItemStack(HorrorEntities.EYE)
    );
    private final ItemStack fiveResult = new ItemStack(HorrorEntities.FIVE);

    public RecipeScreen() {
        super(Component.translatable("screen.horror_mod.stone_lectern"));
    }

    private static ItemStack[] grid(ItemStack... filled) {
        ItemStack[] grid = new ItemStack[9];
        for (int index = 0; index < grid.length; index++) {
            grid[index] = index < filled.length ? filled[index] : ItemStack.EMPTY;
        }
        return grid;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        long clock = this.minecraft.level == null ? 0L : this.minecraft.level.getOverworldClockTime();
        float time = (float) (clock % 100000L) + partialTick;
        drawHovering(graphics, this.veilGrid, this.veilResult, time, 0.0F, 0.32F, 0.28F);
        drawHovering(graphics, this.fiveGrid, this.fiveResult, time, 1.7F, 0.68F, 0.70F);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawHovering(
            GuiGraphicsExtractor graphics,
            ItemStack[] grid,
            ItemStack result,
            float time,
            float phase,
            float anchorX,
            float anchorY
    ) {
        int[] current = panelOrigin(time, phase, anchorX, anchorY);
        int[][] ghosts = new int[TRAIL][];
        float maxDistance = 1.0F;
        for (int ghost = 0; ghost < TRAIL; ghost++) {
            ghosts[ghost] = panelOrigin(time - (ghost + 1) * 3.0F, phase, anchorX, anchorY);
            maxDistance = Math.max(maxDistance, distance(current, ghosts[ghost]));
        }
        for (int ghost = TRAIL - 1; ghost >= 0; ghost--) {
            float away = distance(current, ghosts[ghost]) / maxDistance;
            int alpha = Math.round(24.0F + 196.0F * (1.0F - away));
            drawRecipe(graphics, ghosts[ghost][0], ghosts[ghost][1], rainbow(time - ghost * 6.0F, alpha), grid, result);
        }
        drawRecipe(graphics, current[0], current[1], 0xFFFFFFFF, grid, result);
    }

    private void drawRecipe(GuiGraphicsExtractor graphics, int x, int y, int color, ItemStack[] grid, ItemStack result) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                CRAFTING_TABLE,
                x,
                y,
                CROP_U,
                CROP_V,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                256,
                256,
                color
        );
        int alpha = (color >>> 24) & 0xFF;
        if (alpha < 90) {
            return;
        }
        drawGrid(graphics, x, y, grid);
        graphics.item(result, x + 124 - CROP_U, y + 35 - CROP_V);
    }

    private void drawGrid(GuiGraphicsExtractor graphics, int x, int y, ItemStack[] grid) {
        for (int index = 0; index < grid.length; index++) {
            if (grid[index].isEmpty()) {
                continue;
            }
            int column = index % 3;
            int row = index / 3;
            graphics.item(grid[index], x + 30 - CROP_U + column * 18, y + 17 - CROP_V + row * 18);
        }
    }

    private int[] panelOrigin(float time, float phase, float anchorX, float anchorY) {
        float seconds = time / 20.0F + phase;
        float nx = (float) (Math.sin(seconds * 0.28) * 0.58 + Math.sin(seconds * 0.10 + 0.6) * 0.42);
        float ny = (float) (Math.cos(seconds * 0.22) * 0.55 + Math.sin(seconds * 0.08 + 2.0) * 0.45);
        int spanX = Math.max(1, this.width - IMAGE_WIDTH);
        int spanY = Math.max(1, this.height - IMAGE_HEIGHT);
        int x = Math.round(spanX * (anchorX + nx * 0.10F));
        int y = Math.round(spanY * (anchorY + ny * 0.10F));
        x = Math.max(0, Math.min(spanX, x));
        y = Math.max(0, Math.min(spanY, y));
        return new int[] {x, y};
    }

    private static float distance(int[] a, int[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static int rainbow(float time, int alpha) {
        float hue = Math.floorMod((int) (time * 3.0F), 1000) / 1000.0F;
        int rgb = Color.HSBtoRGB(hue, 0.95F, 1.0F) & 0xFFFFFF;
        return (alpha << 24) | rgb;
    }
}
