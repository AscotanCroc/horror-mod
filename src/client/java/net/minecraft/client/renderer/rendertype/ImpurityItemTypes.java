package net.minecraft.client.renderer.rendertype;

import com.horrormod.client.render.ImpurityItemPipeline;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Item sheet for the detector, so its quads can use the chromatic shader
 * without changing every other item on the atlas.
 */
public final class ImpurityItemTypes {
    public static final RenderType RENDER_TYPE = RenderType.create(
            "horror_impurity_item",
            RenderSetup.builder(ImpurityItemPipeline.WORLD)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup()
    );

    private ImpurityItemTypes() {
    }
}
