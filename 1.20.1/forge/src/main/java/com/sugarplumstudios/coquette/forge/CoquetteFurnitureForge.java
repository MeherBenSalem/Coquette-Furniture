package com.sugarplumstudios.coquette.forge;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.event.CommonEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(CoquetteFurniture.MOD_ID)
public final class CoquetteFurnitureForge {
    public CoquetteFurnitureForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CoquetteFurniture.init();
        ForgePlatform.registerAll(modBus);
        ForgeNetwork.register();

        MinecraftForge.EVENT_BUS.addListener(CoquetteFurnitureForge::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(CoquetteFurnitureForge::onLogin);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.init(modBus);
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
            // players who got their catalog under 1.0's Forge-only persistent data keep counting as served
            CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            if (persistent.getBoolean(CommonEvents.RECEIVED_CATALOG)) {
                player.addTag(CommonEvents.RECEIVED_CATALOG);
            }
            CommonEvents.onLogin(player);
        }
    }
}
