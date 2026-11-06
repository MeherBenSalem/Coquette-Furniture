package com.sugarplumstudios.coquette.fabric;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.client.CoquetteClient;
import com.sugarplumstudios.coquette.client.render.DisplayCabinetRenderer;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;

public final class CoquetteFurnitureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoquetteClient.init();
        KeyBindingHelper.registerKeyBinding(CoquetteClient.OPEN_CATALOG);
        ClientTickEvents.END_CLIENT_TICK.register(client -> CoquetteClient.tick());
        EntityRendererRegistry.register(ModEntities.SEAT.get(), NoopRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DISPLAY_CABINET.get(), DisplayCabinetRenderer::new);
        // Forge reads "render_type": "cutout" from the block models; Fabric needs to be told.
        BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(CoquetteFurniture.MOD_ID))
                .forEach(entry -> BlockRenderLayerMap.INSTANCE.putBlock(entry.getValue(), RenderType.cutout()));
    }
}
