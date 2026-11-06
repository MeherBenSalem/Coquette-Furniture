package com.sugarplumstudios.coquette.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.dev.ShowcaseHarness;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Client set-up shared by every loader. The loaders register the key category and {@link #OPEN_CATALOG}
 * (see {@link #createKeyMapping}), the renderers and the tick hook.
 */
public final class CoquetteClient {
    /** The key category; its label is the lang key {@code key.category.coquettefurniture.main}. */
    public static final Identifier KEY_CATEGORY = CoquetteFurniture.id("main");
    public static KeyMapping OPEN_CATALOG;

    private CoquetteClient() {
    }

    public static void init() {
        CoquetteConfig.loadClient();
        CoquetteFurniture.openCatalog = ClientAccess::openCatalog;
    }

    /** Creates the catalog key in the category the loader registered. */
    public static KeyMapping createKeyMapping(KeyMapping.Category category) {
        OPEN_CATALOG = new KeyMapping("key.coquettefurniture.catalog", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category);
        return OPEN_CATALOG;
    }

    /** Call at the end of every client tick. */
    public static void tick() {
        if (ShowcaseHarness.ENABLED) {
            ShowcaseHarness.onTick();
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_CATALOG != null && OPEN_CATALOG.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                ClientAccess.openCatalog();
            }
        }
    }
}
