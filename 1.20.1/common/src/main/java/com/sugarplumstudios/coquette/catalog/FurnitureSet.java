package com.sugarplumstudios.coquette.catalog;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.sugarplumstudios.coquette.block.FurnitureColor.BABY_BLUE;
import static com.sugarplumstudios.coquette.block.FurnitureColor.BABY_PINK;
import static com.sugarplumstudios.coquette.block.FurnitureColor.CREAM;
import static com.sugarplumstudios.coquette.block.FurnitureColor.LAVENDER;
import static com.sugarplumstudios.coquette.block.FurnitureColor.MINT;
import static com.sugarplumstudios.coquette.block.FurnitureColor.WHITE;
import static com.sugarplumstudios.coquette.block.FurnitureType.*;

/** Themed collections shown in the catalog; every piece also lists its sets in its tooltip. */
public enum FurnitureSet {
    PRINCESS_BEDROOM(0xF7B8CF,
            f(CANOPY_BED, BABY_PINK), f(DECORATIVE_PILLOW, BABY_PINK), f(BEDSIDE_TABLE, WHITE), f(WARDROBE, BABY_PINK),
            f(DRESSER, WHITE), f(VANITY_TABLE, BABY_PINK), f(STANDING_MIRROR, WHITE), f(JEWELRY_BOX, BABY_PINK),
            f(NIGHT_LAMP, BABY_PINK), f(RUG, BABY_PINK), f(CURTAINS, BABY_PINK), f(ARMCHAIR, WHITE),
            d(DecorType.PERFUME_BOTTLES), d(DecorType.TEDDY_BEAR), d(DecorType.HEART_PHOTO_FRAME), d(DecorType.PASTEL_CANDLES),
            d(DecorType.POTTED_ROSE_BUSH)),
    STRAWBERRY_COTTAGE(0xF2A3BA,
            f(KITCHEN_COUNTER, CREAM), f(KITCHEN_CABINET, CREAM), f(KITCHEN_SINK, BABY_PINK), f(STOVE, CREAM),
            f(DINING_TABLE, CREAM), f(DINING_CHAIR, BABY_PINK), f(TEA_SET, BABY_PINK), f(PICNIC_BLANKET, BABY_PINK),
            f(GARDEN_BENCH, CREAM), f(FLOWER_POT, BABY_PINK), f(BIRDHOUSE, CREAM), f(PICKET_FENCE, WHITE),
            f(SWING_SEAT, BABY_PINK), f(OUTDOOR_TABLE, CREAM), d(DecorType.STRAWBERRY_BASKET), d(DecorType.CUPCAKE_STAND),
            d(DecorType.TULIP_VASE)),
    LAVENDER_DREAMS(0xCFBDF2,
            f(CANOPY_BED, LAVENDER), f(DECORATIVE_PILLOW, LAVENDER), f(BEDSIDE_TABLE, LAVENDER), f(WARDROBE, LAVENDER),
            f(NIGHT_LAMP, LAVENDER), f(RUG, LAVENDER), f(CURTAINS, LAVENDER), f(ARMCHAIR, LAVENDER), f(BATHTUB, LAVENDER),
            f(BATHROOM_SINK, LAVENDER), f(WALL_MIRROR, LAVENDER), f(TOWEL_RACK, LAVENDER), f(SOAP_DISPENSER, LAVENDER),
            d(DecorType.POTTED_LAVENDER), d(DecorType.BOOK_STACK), d(DecorType.HEART_MUSIC_BOX)),
    ANGELCORE_ROOM(0xB9DAF7,
            f(CANOPY_BED, WHITE), f(DECORATIVE_PILLOW, BABY_BLUE), f(WARDROBE, WHITE), f(VANITY_TABLE, WHITE),
            f(STANDING_MIRROR, BABY_BLUE), f(BOOKSHELF, WHITE), f(SOFA, BABY_BLUE), f(COFFEE_TABLE, WHITE),
            f(DISPLAY_CABINET, WHITE), f(RUG, BABY_BLUE), f(CURTAINS, WHITE), f(NIGHT_LAMP, BABY_BLUE),
            d(DecorType.ANGEL_FIGURINE), d(DecorType.BUNNY_PLUSH), d(DecorType.ROSE_VASE), d(DecorType.PASTEL_CANDLES)),
    PASTEL_KITCHEN(0xB6EAD2,
            f(REFRIGERATOR, MINT), f(STOVE, BABY_BLUE), f(KITCHEN_COUNTER, CREAM), f(KITCHEN_CABINET, MINT),
            f(KITCHEN_SINK, WHITE), f(DINING_TABLE, LAVENDER), f(DINING_CHAIR, BABY_PINK), f(DINING_CHAIR, MINT),
            f(TEA_SET, MINT), f(DISPLAY_CABINET, BABY_BLUE), d(DecorType.CUPCAKE_STAND), d(DecorType.TULIP_VASE),
            d(DecorType.RIBBON_GIFT_BOX));

    private final String id = name().toLowerCase(Locale.ROOT);
    private final int color;
    private final List<Supplier<Item>> pieces;

    @SafeVarargs
    FurnitureSet(int color, Supplier<Item>... pieces) {
        this.color = color;
        this.pieces = List.of(pieces);
    }

    private static Supplier<Item> f(FurnitureType type, FurnitureColor color) {
        return () -> ModItems.furniture(type, color);
    }

    private static Supplier<Item> d(DecorType decor) {
        return () -> ModItems.decor(decor);
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "set.coquettefurniture." + id;
    }

    public int color() {
        return color;
    }

    public List<Item> pieces() {
        List<Item> items = new ArrayList<>();
        for (Supplier<Item> piece : pieces) {
            items.add(piece.get());
        }
        return items;
    }

    private static Map<Item, List<FurnitureSet>> byItem;

    public static List<FurnitureSet> setsContaining(Item item) {
        if (byItem == null) {
            Map<Item, List<FurnitureSet>> map = new IdentityHashMap<>();
            for (FurnitureSet set : values()) {
                for (Item piece : set.pieces()) {
                    List<FurnitureSet> sets = map.computeIfAbsent(piece, k -> new ArrayList<>());
                    if (!sets.contains(set)) {
                        sets.add(set);
                    }
                }
            }
            byItem = map;
        }
        return byItem.getOrDefault(item, List.of());
    }
}
