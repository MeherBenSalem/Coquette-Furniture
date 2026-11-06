package com.sugarplumstudios.coquette.client;

import com.sugarplumstudios.coquette.client.screen.CatalogScreen;
import net.minecraft.client.Minecraft;

/** Client-only entry points. */
public final class ClientAccess {
    private ClientAccess() {
    }

    public static void openCatalog() {
        Minecraft.getInstance().gui.setScreen(new CatalogScreen());
    }
}
