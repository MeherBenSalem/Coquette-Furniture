package com.sugarplumstudios.coquette.client;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CoquetteFurniture.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (ClientModEvents.OPEN_CATALOG.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                ClientAccess.openCatalog();
            }
        }
    }
}
