package com.sugarplumstudios.coquette.neoforge;

import com.sugarplumstudios.coquette.client.CoquetteClient;
import com.sugarplumstudios.coquette.client.render.DisplayCabinetRenderer;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        CoquetteClient.init();
        CoquetteClient.OPEN_CATALOG.setKeyConflictContext(KeyConflictContext.IN_GAME);
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY_CABINET.get(), DisplayCabinetRenderer::new);
        });
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(CoquetteClient.OPEN_CATALOG));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> CoquetteClient.tick());
    }
}
