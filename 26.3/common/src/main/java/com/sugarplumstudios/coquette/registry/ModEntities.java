package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.entity.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class ModEntities {
    public static final RegistryEntry<EntityType<SeatEntity>> SEAT = Services.PLATFORM.register(Registries.ENTITY_TYPE, "seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).noSummon().noSave()
                    .fireImmune().clientTrackingRange(10).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, CoquetteFurniture.id("seat"))));

    private ModEntities() {
    }

    public static void init() {
    }
}
