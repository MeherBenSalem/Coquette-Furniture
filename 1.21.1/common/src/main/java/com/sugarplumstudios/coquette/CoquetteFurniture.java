package com.sugarplumstudios.coquette;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModBlocks;
import com.sugarplumstudios.coquette.registry.ModCreativeTabs;
import com.sugarplumstudios.coquette.registry.ModEntities;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/** Loader-independent entry point. Each loader calls {@link #init()} from its own mod constructor or initializer. */
public final class CoquetteFurniture {
    public static final String MOD_ID = "coquettefurniture";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Set by the client entry point; opens the Interior Design Catalog screen. */
    public static Runnable openCatalog = () -> {
    };

    private CoquetteFurniture() {
    }

    /** Reads the config and registers every block, item, block entity, entity and creative tab. */
    public static void init() {
        CoquetteConfig.loadCommon();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModCreativeTabs.init();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
