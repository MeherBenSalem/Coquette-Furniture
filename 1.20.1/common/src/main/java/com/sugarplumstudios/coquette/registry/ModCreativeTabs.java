package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.Room;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.Registries;

public final class ModCreativeTabs {
    public static final RegistryEntry<CreativeModeTab> FURNITURE = Services.PLATFORM.register(Registries.CREATIVE_MODE_TAB, "furniture", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.coquettefurniture"))
            .icon(() -> new ItemStack(ModItems.furniture(FurnitureType.CANOPY_BED, FurnitureColor.BABY_PINK)))
            .displayItems((params, output) -> {
                output.accept(ModItems.INTERIOR_DESIGN_CATALOG.get());
                for (Room room : Room.values()) {
                    for (FurnitureType type : FurnitureType.values()) {
                        if (type.room() != room) {
                            continue;
                        }
                        for (FurnitureColor color : FurnitureColor.values()) {
                            output.accept(ModItems.furniture(type, color));
                        }
                    }
                }
            })
            .build());

    public static final RegistryEntry<CreativeModeTab> DECOR = Services.PLATFORM.register(Registries.CREATIVE_MODE_TAB, "decor", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.coquettefurniture.decor"))
            .icon(() -> new ItemStack(ModItems.decor(DecorType.TEDDY_BEAR)))
            .displayItems((params, output) -> {
                output.accept(ModItems.INTERIOR_DESIGN_CATALOG.get());
                for (DecorType decor : DecorType.values()) {
                    output.accept(ModItems.decor(decor));
                }
                output.accept(ModItems.CUP_OF_TEA.get());
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void init() {
    }
}
