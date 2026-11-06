package com.sugarplumstudios.coquette;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.network.ModNetwork;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModBlocks;
import com.sugarplumstudios.coquette.registry.ModCreativeTabs;
import com.sugarplumstudios.coquette.registry.ModEntities;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CoquetteFurniture.MOD_ID)
public class CoquetteFurniture {
    public static final String MOD_ID = "coquettefurniture";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CoquetteFurniture() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        ModNetwork.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoquetteConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CoquetteConfig.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
