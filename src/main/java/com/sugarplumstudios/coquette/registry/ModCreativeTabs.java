package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.Room;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CoquetteFurniture.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FURNITURE = TABS.register("furniture", () -> CreativeModeTab.builder()
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

    public static final RegistryObject<CreativeModeTab> DECOR = TABS.register("decor", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.coquettefurniture.decor"))
            .icon(() -> new ItemStack(ModItems.decor(DecorType.TEDDY_BEAR)))
            .withTabsBefore(FURNITURE.getKey())
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
}
