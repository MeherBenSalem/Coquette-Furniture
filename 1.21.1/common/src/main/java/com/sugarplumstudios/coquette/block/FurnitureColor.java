package com.sugarplumstudios.coquette.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** The six pastel colourways every suitable piece of furniture comes in. */
public enum FurnitureColor implements StringRepresentable {
    BABY_PINK(MapColor.COLOR_PINK, DyeColor.PINK, 0xF7B8CF),
    WHITE(MapColor.SNOW, DyeColor.WHITE, 0xF8F4F1),
    CREAM(MapColor.SAND, DyeColor.YELLOW, 0xF5E5C6),
    LAVENDER(MapColor.COLOR_PURPLE, DyeColor.PURPLE, 0xCFBDF2),
    MINT(MapColor.COLOR_LIGHT_GREEN, DyeColor.LIME, 0xB6EAD2),
    BABY_BLUE(MapColor.COLOR_LIGHT_BLUE, DyeColor.LIGHT_BLUE, 0xB9DAF7);

    private final String id;
    private final MapColor mapColor;
    private final DyeColor dye;
    private final int rgb;

    FurnitureColor(MapColor mapColor, DyeColor dye, int rgb) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.mapColor = mapColor;
        this.dye = dye;
        this.rgb = rgb;
    }

    public String id() {
        return id;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    /** The vanilla dye used to craft and recolour this colourway. */
    public DyeColor dye() {
        return dye;
    }

    public int rgb() {
        return rgb;
    }

    public String translationKey() {
        return "color.coquettefurniture." + id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    @Nullable
    public static FurnitureColor fromDye(DyeColor dye) {
        for (FurnitureColor color : values()) {
            if (color.dye == dye) {
                return color;
            }
        }
        return null;
    }
}
