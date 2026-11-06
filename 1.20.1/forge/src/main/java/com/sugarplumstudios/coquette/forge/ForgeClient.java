package com.sugarplumstudios.coquette.forge;

import com.sugarplumstudios.coquette.client.CoquetteClient;
import com.sugarplumstudios.coquette.client.render.DisplayCabinetRenderer;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

final class ForgeClient {
    private ForgeClient() {
    }

    static void init(IEventBus modBus) {
        CoquetteClient.init();
        CoquetteClient.OPEN_CATALOG.setKeyConflictContext(KeyConflictContext.IN_GAME);
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY_CABINET.get(), DisplayCabinetRenderer::new);
        });
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(CoquetteClient.OPEN_CATALOG));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                CoquetteClient.tick();
            }
        });
    }
}
