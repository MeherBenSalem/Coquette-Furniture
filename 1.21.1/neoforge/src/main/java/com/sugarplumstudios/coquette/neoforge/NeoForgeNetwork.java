package com.sugarplumstudios.coquette.neoforge;

import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class NeoForgeNetwork {
    private NeoForgeNetwork() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CatalogTakePacket.PROTOCOL);
        // Handlers run on the server's main thread.
        registrar.playToServer(CatalogTakePacket.TYPE, CatalogTakePacket.STREAM_CODEC, (packet, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                CatalogTakePacket.handle(packet, player);
            }
        });
    }
}
