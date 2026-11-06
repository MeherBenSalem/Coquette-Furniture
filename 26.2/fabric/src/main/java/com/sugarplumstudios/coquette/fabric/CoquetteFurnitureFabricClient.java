package com.sugarplumstudios.coquette.fabric;

import com.sugarplumstudios.coquette.client.CoquetteClient;
import com.sugarplumstudios.coquette.client.render.BlockPreviewRenderer;
import com.sugarplumstudios.coquette.client.render.DisplayCabinetRenderer;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Fabric client set-up. Since 26.1 vanilla picks each block quad's render layer (solid, cutout, translucent) from
 * its texture, so the furniture needs no BlockRenderLayerMap entries any more.
 */
public final class CoquetteFurnitureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoquetteClient.init();
        // GuiGraphicsExtractor.guiRenderState is made accessible by coquettefurniture.accesswidener
        CoquetteClient.pictureInPictureSubmitter = (graphics, state) -> graphics.guiRenderState.addPicturesInPictureState(state);
        KeyMappingHelper.registerKeyMapping(CoquetteClient.createKeyMapping(KeyMapping.Category.register(CoquetteClient.KEY_CATEGORY)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> CoquetteClient.tick());
        EntityRendererRegistry.register(ModEntities.SEAT.get(), NoopRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.DISPLAY_CABINET.get(), DisplayCabinetRenderer::new);
        PictureInPictureRendererRegistry.register(context -> new BlockPreviewRenderer());
    }
}
