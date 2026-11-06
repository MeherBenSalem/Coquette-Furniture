package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A painted bookshelf full of pastel books; it powers enchanting tables like a vanilla bookshelf. */
public class BookshelfBlock extends FurnitureBlock {
    public static boolean powersEnchanting() {
        return CoquetteConfig.COMMON.bookshelfEnchanting.get();
    }

    public BookshelfBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, false, 0);
    }

    /** Forge and NeoForge call this (it is not a vanilla method); Fabric uses a mixin, see EnchantingBookshelves. */
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return CoquetteConfig.COMMON.bookshelfEnchanting.get() ? 1.0F : 0.0F;
    }
}
