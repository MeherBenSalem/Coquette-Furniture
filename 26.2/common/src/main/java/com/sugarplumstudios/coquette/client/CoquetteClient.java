package com.sugarplumstudios.coquette.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.dev.ShowcaseHarness;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

/**
 * Client set-up shared by every loader. The loaders register the key category and {@link #OPEN_CATALOG}
 * (see {@link #createKeyMapping}), the renderers and the tick hook.
 */
public final class CoquetteClient {
    /** The key category; its label is the lang key {@code key.category.coquettefurniture.main}. */
    public static final Identifier KEY_CATEGORY = CoquetteFurniture.id("main");
    public static KeyMapping OPEN_CATALOG;
    /** Read here rather than from ShowcaseHarness: release jars leave the dev package out. */
    private static final boolean SHOWCASE = Boolean.getBoolean("coquettefurniture.showcase");
    /**
     * Hands a custom picture-in-picture render state to the GUI renderer. Vanilla keeps the render state list
     * private, so each loader supplies this (NeoForge has a method for it, Fabric uses an access widener).
     */
    public static BiConsumer<GuiGraphicsExtractor, PictureInPictureRenderState> pictureInPictureSubmitter = (g, state) -> {
    };

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

    public static void submitPictureInPicture(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
        pictureInPictureSubmitter.accept(graphics, state);
    }

    /** Call at the end of every client tick. */
    public static void tick() {
        if (SHOWCASE) {
            ShowcaseHarness.onTick();
        }
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_CATALOG != null && OPEN_CATALOG.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                ClientAccess.openCatalog();
            }
        }
    }
}
