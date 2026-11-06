package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.item.CatalogItem;
import com.sugarplumstudios.coquette.item.FurnitureItem;
import com.sugarplumstudios.coquette.item.TeaCupItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.Registries;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final Map<FurnitureType, Map<FurnitureColor, RegistryEntry<Item>>> FURNITURE = new EnumMap<>(FurnitureType.class);
    public static final Map<DecorType, RegistryEntry<Item>> DECOR = new EnumMap<>(DecorType.class);

    static {
        for (FurnitureType type : FurnitureType.values()) {
            Map<FurnitureColor, RegistryEntry<Item>> colors = new EnumMap<>(FurnitureColor.class);
            for (FurnitureColor color : FurnitureColor.values()) {
                colors.put(color, Services.PLATFORM.register(Registries.ITEM, type.blockId(color), () -> new FurnitureItem(ModBlocks.furniture(type, color),
                        new Item.Properties(), type.features(), type.storageSlots(), true)));
            }
            FURNITURE.put(type, colors);
        }
        for (DecorType decor : DecorType.values()) {
            DECOR.put(decor, Services.PLATFORM.register(Registries.ITEM, decor.id(), () -> new FurnitureItem(ModBlocks.decor(decor), new Item.Properties(),
                    decor.features(), 0, false)));
        }
    }

    public static final RegistryEntry<Item> INTERIOR_DESIGN_CATALOG = Services.PLATFORM.register(Registries.ITEM, "interior_design_catalog",
            () -> new CatalogItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryEntry<Item> CUP_OF_TEA = Services.PLATFORM.register(Registries.ITEM, "cup_of_tea",
            () -> new TeaCupItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(2)
                    .saturationMod(0.6F).alwaysEat().effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                    .build())));

    private ModItems() {
    }

    /** Loads this class, registering every item. */
    public static void init() {
    }

    public static Item furniture(FurnitureType type, FurnitureColor color) {
        return FURNITURE.get(type).get(color).get();
    }

    public static Item decor(DecorType decor) {
        return DECOR.get(decor).get();
    }
}
