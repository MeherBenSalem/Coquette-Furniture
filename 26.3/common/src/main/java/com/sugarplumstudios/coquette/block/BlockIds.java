package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Since 1.21.2 block properties must carry the block's registry id. The furniture and decor enums build their
 * properties in shared helpers, so the id of the block being created is handed to them through here.
 */
final class BlockIds {
    private static ResourceKey<Block> pending;

    private BlockIds() {
    }

    /** Runs a block factory with {@code name} as the id that {@link #props()} hands out. */
    static synchronized Block create(String name, Supplier<Block> factory) {
        pending = ResourceKey.create(Registries.BLOCK, CoquetteFurniture.id(name));
        try {
            return factory.get();
        } finally {
            pending = null;
        }
    }

    /** Fresh block properties carrying the id of the block being created. */
    static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().setId(Objects.requireNonNull(pending, "no block is being created"));
    }
}
