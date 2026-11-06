package com.sugarplumstudios.coquette.neoforge;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(CoquetteFurniture.MOD_ID)
public final class CoquetteFurnitureNeoForge {
    public CoquetteFurnitureNeoForge(IEventBus modBus) {
        CoquetteFurniture.init();
        NeoForgePlatform.registerAll(modBus);
        modBus.addListener(NeoForgeNetwork::register);

        NeoForge.EVENT_BUS.addListener(CoquetteFurnitureNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(CoquetteFurnitureNeoForge::onLogin);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClient.init(modBus);
        }
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
