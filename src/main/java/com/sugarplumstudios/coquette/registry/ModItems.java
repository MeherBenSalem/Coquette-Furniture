package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.CoquetteFurniture;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CoquetteFurniture.MOD_ID);

    public static final Map<FurnitureType, Map<FurnitureColor, RegistryObject<Item>>> FURNITURE = new EnumMap<>(FurnitureType.class);
    public static final Map<DecorType, RegistryObject<Item>> DECOR = new EnumMap<>(DecorType.class);

    static {
        for (FurnitureType type : FurnitureType.values()) {
            Map<FurnitureColor, RegistryObject<Item>> colors = new EnumMap<>(FurnitureColor.class);
            for (FurnitureColor color : FurnitureColor.values()) {
                colors.put(color, ITEMS.register(type.blockId(color), () -> new FurnitureItem(ModBlocks.furniture(type, color),
                        new Item.Properties(), type.features(), type.storageSlots(), true)));
            }
            FURNITURE.put(type, colors);
        }
        for (DecorType decor : DecorType.values()) {
            DECOR.put(decor, ITEMS.register(decor.id(), () -> new FurnitureItem(ModBlocks.decor(decor), new Item.Properties(),
                    decor.features(), 0, false)));
        }
    }

    public static final RegistryObject<Item> INTERIOR_DESIGN_CATALOG = ITEMS.register("interior_design_catalog",
            () -> new CatalogItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CUP_OF_TEA = ITEMS.register("cup_of_tea",
            () -> new TeaCupItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(2)
                    .saturationMod(0.6F).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                    .build())));

    private ModItems() {
    }

    public static Item furniture(FurnitureType type, FurnitureColor color) {
        return FURNITURE.get(type).get(color).get();
    }

    public static Item decor(DecorType decor) {
        return DECOR.get(decor).get();
    }
}
