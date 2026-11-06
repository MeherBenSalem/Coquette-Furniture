package com.sugarplumstudios.coquette.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** What a piece of furniture can do; shown in tooltips and the catalog. */
public enum Feature {
    SEAT,
    STORAGE,
    BED,
    LIGHT,
    SURFACE,
    WATER,
    COOKING,
    DISPLAY,
    CONNECTS,
    ENCHANTING,
    PAMPER,
    MUSIC,
    TEA,
    TOGGLE,
    BATH,
    DECOR,
    STACK,
    DYE,
    BIRDS,
    CANDLES;

    private final String key = "tooltip.coquettefurniture.feature." + name().toLowerCase(Locale.ROOT);

    public Component describe(int storageSlots) {
        Component text = this == STORAGE ? Component.translatable(key, storageSlots) : Component.translatable(key);
        return Component.literal("• ").append(text).withStyle(ChatFormatting.GRAY);
    }
}
