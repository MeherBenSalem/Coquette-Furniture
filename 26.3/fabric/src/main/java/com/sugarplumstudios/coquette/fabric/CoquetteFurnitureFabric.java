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

        PayloadTypeRegistry.serverboundPlay().register(CatalogTakePacket.TYPE, CatalogTakePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CatalogTakePacket.TYPE,
                (packet, context) -> context.server().execute(() -> CatalogTakePacket.handle(packet, context.player())));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            InteractionResult result = CommonEvents.onUseBlock(player, level, hand, hit.getBlockPos());
            return result == null ? InteractionResult.PASS : result;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onLogin(handler.player));
    }
}
