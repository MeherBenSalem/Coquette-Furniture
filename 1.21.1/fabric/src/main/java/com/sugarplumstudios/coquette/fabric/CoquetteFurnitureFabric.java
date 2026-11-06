package com.sugarplumstudios.coquette.fabric;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.event.CommonEvents;
import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.InteractionResult;

public final class CoquetteFurnitureFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CoquetteFurniture.init();

        PayloadTypeRegistry.playC2S().register(CatalogTakePacket.TYPE, CatalogTakePacket.STREAM_CODEC);
        // Handlers run on the server's main thread.
        ServerPlayNetworking.registerGlobalReceiver(CatalogTakePacket.TYPE, (packet, context) -> CatalogTakePacket.handle(packet, context.player()));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            InteractionResult result = CommonEvents.onUseBlock(player, level, hand, hit.getBlockPos());
            return result == null ? InteractionResult.PASS : result;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onLogin(handler.player));
    }
}
