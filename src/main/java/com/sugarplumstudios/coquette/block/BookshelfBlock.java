package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A painted bookshelf full of pastel books; it powers enchanting tables like a vanilla bookshelf. */
public class BookshelfBlock extends FurnitureBlock {
    public BookshelfBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, false, 0);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return CoquetteConfig.COMMON.bookshelfEnchanting.get() ? 1.0F : 0.0F;
    }
}
