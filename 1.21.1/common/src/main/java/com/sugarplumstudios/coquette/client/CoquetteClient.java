package com.sugarplumstudios.coquette.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.dev.ShowcaseHarness;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Client set-up shared by every loader. The loaders register {@link #OPEN_CATALOG}, the renderers and the tick hook. */
public final class CoquetteClient {
    public static final KeyMapping OPEN_CATALOG = new KeyMapping("key.coquettefurniture.catalog",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.coquettefurniture");

    /** Read here, not from ShowcaseHarness, because release jars leave out the dev package. */
    private static final boolean SHOWCASE = Boolean.getBoolean("coquettefurniture.showcase");

    private CoquetteClient() {
    }

    public static void init() {
        CoquetteConfig.loadClient();
        CoquetteFurniture.openCatalog = ClientAccess::openCatalog;
    }

    /** Call at the end of every client tick. */
    public static void tick() {
        if (SHOWCASE) {
            ShowcaseHarness.onTick();
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_CATALOG.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                ClientAccess.openCatalog();
            }
        }
    }
}
