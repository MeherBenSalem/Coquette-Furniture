package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import com.sugarplumstudios.coquette.block.entity.MusicBoxBlockEntity;
import com.sugarplumstudios.coquette.block.entity.StoveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CoquetteFurniture.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<FurnitureStorageBlockEntity>> FURNITURE_STORAGE = BLOCK_ENTITIES.register(
            "furniture_storage", () -> BlockEntityType.Builder.of(FurnitureStorageBlockEntity::new, ModBlocks.allOf(
                    FurnitureType.BEDSIDE_TABLE, FurnitureType.WARDROBE, FurnitureType.DRESSER, FurnitureType.VANITY_TABLE,
                    FurnitureType.JEWELRY_BOX, FurnitureType.KITCHEN_CABINET, FurnitureType.REFRIGERATOR,
                    FurnitureType.KITCHEN_COUNTER, FurnitureType.BATHROOM_CABINET)).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<StoveBlockEntity>> STOVE = BLOCK_ENTITIES.register("stove",
            () -> BlockEntityType.Builder.of(StoveBlockEntity::new, ModBlocks.allOf(FurnitureType.STOVE)).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<DisplayCabinetBlockEntity>> DISPLAY_CABINET = BLOCK_ENTITIES.register(
            "display_cabinet", () -> BlockEntityType.Builder.of(DisplayCabinetBlockEntity::new,
                    ModBlocks.allOf(FurnitureType.DISPLAY_CABINET)).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<MusicBoxBlockEntity>> MUSIC_BOX = BLOCK_ENTITIES.register("music_box",
            () -> BlockEntityType.Builder.of(MusicBoxBlockEntity::new, ModBlocks.decor(DecorType.HEART_MUSIC_BOX)).build(null));

    private ModBlockEntities() {
    }
}
