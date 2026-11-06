package com.sugarplumstudios.coquette.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.client.render.DisplayCabinetRenderer;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CoquetteFurniture.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping OPEN_CATALOG = new KeyMapping("key.coquettefurniture.catalog", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.coquettefurniture");

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY_CABINET.get(), DisplayCabinetRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CATALOG);
    }
}
