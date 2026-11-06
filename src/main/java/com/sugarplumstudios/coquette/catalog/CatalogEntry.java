package com.sugarplumstudios.coquette.catalog;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.Feature;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.Room;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One piece of furniture as listed in the Interior Design Catalog. */
public record CatalogEntry(Item item, Room room, @Nullable FurnitureColor color, List<Feature> features, int storageSlots) {
    private static List<CatalogEntry> all;

    /** Every piece, grouped by room in display order. */
    public static List<CatalogEntry> all() {
        if (all == null) {
            List<CatalogEntry> entries = new ArrayList<>();
            for (Room room : Room.values()) {
                for (FurnitureType type : FurnitureType.values()) {
                    if (type.room() != room) {
                        continue;
                    }
                    for (FurnitureColor color : FurnitureColor.values()) {
                        entries.add(new CatalogEntry(ModItems.furniture(type, color), room, color, withDye(type.features()),
                                type.storageSlots()));
                    }
                }
                for (DecorType decor : DecorType.values()) {
                    if (decor.room() == room) {
                        entries.add(new CatalogEntry(ModItems.decor(decor), room, null, decor.features(), 0));
                    }
                }
            }
            all = List.copyOf(entries);
        }
        return all;
    }

    private static List<Feature> withDye(List<Feature> features) {
        List<Feature> out = new ArrayList<>(features);
        out.add(Feature.DYE);
        return out;
    }
}
