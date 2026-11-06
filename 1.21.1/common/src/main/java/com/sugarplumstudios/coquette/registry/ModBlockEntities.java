package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import com.sugarplumstudios.coquette.block.entity.MusicBoxBlockEntity;
import com.sugarplumstudios.coquette.block.entity.StoveBlockEntity;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final RegistryEntry<BlockEntityType<FurnitureStorageBlockEntity>> FURNITURE_STORAGE = Services.PLATFORM.register(
            Registries.BLOCK_ENTITY_TYPE, "furniture_storage", () -> Services.PLATFORM.blockEntityType(FurnitureStorageBlockEntity::new,
                    ModBlocks.allOf(FurnitureType.BEDSIDE_TABLE, FurnitureType.WARDROBE, FurnitureType.DRESSER, FurnitureType.VANITY_TABLE,
                            FurnitureType.JEWELRY_BOX, FurnitureType.KITCHEN_CABINET, FurnitureType.REFRIGERATOR,
                            FurnitureType.KITCHEN_COUNTER, FurnitureType.BATHROOM_CABINET)));

    public static final RegistryEntry<BlockEntityType<StoveBlockEntity>> STOVE = Services.PLATFORM.register(Registries.BLOCK_ENTITY_TYPE,
            "stove", () -> Services.PLATFORM.blockEntityType(StoveBlockEntity::new, ModBlocks.allOf(FurnitureType.STOVE)));

    public static final RegistryEntry<BlockEntityType<DisplayCabinetBlockEntity>> DISPLAY_CABINET = Services.PLATFORM.register(
            Registries.BLOCK_ENTITY_TYPE, "display_cabinet",
            () -> Services.PLATFORM.blockEntityType(DisplayCabinetBlockEntity::new, ModBlocks.allOf(FurnitureType.DISPLAY_CABINET)));

    public static final RegistryEntry<BlockEntityType<MusicBoxBlockEntity>> MUSIC_BOX = Services.PLATFORM.register(Registries.BLOCK_ENTITY_TYPE,
            "music_box", () -> Services.PLATFORM.blockEntityType(MusicBoxBlockEntity::new, ModBlocks.decor(DecorType.HEART_MUSIC_BOX)));

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
