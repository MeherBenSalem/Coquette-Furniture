package com.sugarplumstudios.coquette.neoforge;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.event.CommonEvents;
import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(CoquetteFurniture.MOD_ID)
public final class CoquetteFurnitureNeoForge {
    private static final String PROTOCOL = "1";

    public CoquetteFurnitureNeoForge(IEventBus modBus) {
        CoquetteFurniture.init();
        NeoForgePlatform.registerAll(modBus);
        modBus.addListener(CoquetteFurnitureNeoForge::registerPayloads);

        NeoForge.EVENT_BUS.addListener(CoquetteFurnitureNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(CoquetteFurnitureNeoForge::onLogin);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClient.init(modBus);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // handlers run on the server thread
        event.registrar(PROTOCOL).playToServer(CatalogTakePacket.TYPE, CatalogTakePacket.STREAM_CODEC,
                (packet, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CatalogTakePacket.handle(packet, player);
                    }
                });
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = CommonEvents.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getPos());
        if (result != null) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onLogin(player);
        }
    }
}
