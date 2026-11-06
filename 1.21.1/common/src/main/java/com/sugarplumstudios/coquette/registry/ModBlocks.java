package com.sugarplumstudios.coquette.registry;

import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import net.minecraft.world.level.block.Block;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ModBlocks {
    public static final Map<FurnitureType, Map<FurnitureColor, RegistryEntry<Block>>> FURNITURE = new EnumMap<>(FurnitureType.class);
    public static final Map<DecorType, RegistryEntry<Block>> DECOR = new EnumMap<>(DecorType.class);

    static {
        for (FurnitureType type : FurnitureType.values()) {
            Map<FurnitureColor, RegistryEntry<Block>> colors = new EnumMap<>(FurnitureColor.class);
            for (FurnitureColor color : FurnitureColor.values()) {
                colors.put(color, Services.PLATFORM.register(Registries.BLOCK, type.blockId(color), () -> type.create(color)));
            }
            FURNITURE.put(type, colors);
        }
        for (DecorType decor : DecorType.values()) {
            DECOR.put(decor, Services.PLATFORM.register(Registries.BLOCK, decor.id(), decor::create));
        }
    }

    private static Map<Block, FurnitureType> typeLookup;
    private static Map<Block, FurnitureColor> colorLookup;

    private ModBlocks() {
    }

    /** Loads this class, registering every block. */
    public static void init() {
    }

    public static Block furniture(FurnitureType type, FurnitureColor color) {
        return FURNITURE.get(type).get(color).get();
    }

    public static Block decor(DecorType decor) {
        return DECOR.get(decor).get();
    }

    @Nullable
    public static FurnitureType typeOf(Block block) {
        buildLookups();
        return typeLookup.get(block);
    }

    @Nullable
    public static FurnitureColor colorOf(Block block) {
        buildLookups();
        return colorLookup.get(block);
    }

    /** Every block of the given furniture types, in all colours. */
    public static Block[] allOf(FurnitureType... types) {
        List<Block> blocks = new ArrayList<>();
        for (FurnitureType type : types) {
            for (FurnitureColor color : FurnitureColor.values()) {
                blocks.add(furniture(type, color));
            }
        }
        return blocks.toArray(Block[]::new);
    }

    private static synchronized void buildLookups() {
        if (typeLookup != null) {
            return;
        }
        Map<Block, FurnitureType> types = new IdentityHashMap<>();
        Map<Block, FurnitureColor> colors = new IdentityHashMap<>();
        FURNITURE.forEach((type, byColor) -> byColor.forEach((color, block) -> {
            types.put(block.get(), type);
            colors.put(block.get(), color);
        }));
        colorLookup = colors;
        typeLookup = types;
    }
}
