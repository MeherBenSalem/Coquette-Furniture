package com.sugarplumstudios.coquette.block;

import java.util.Locale;

/** Catalog and creative tab grouping. */
public enum Room {
    BEDROOM,
    LIVING_ROOM,
    KITCHEN,
    BATHROOM,
    GARDEN,
    DECOR;

    private final String id = name().toLowerCase(Locale.ROOT);

    public String id() {
        return id;
    }

    public String translationKey() {
        return "room.coquettefurniture." + id;
    }
}
