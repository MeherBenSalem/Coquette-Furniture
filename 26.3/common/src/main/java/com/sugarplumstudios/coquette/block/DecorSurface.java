package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Something decorations can stand on. Decorations sink down in 2 pixel steps so
 * they sit right on top of tables, counters, beds and seats that are lower than a
 * full block.
 */
public interface DecorSurface {
    int NOT_A_SURFACE = -1;
    int MAX_SINK = 4;

    /** How many 2 pixel steps a decoration above this block sinks, or {@link #NOT_A_SURFACE}. */
    int decorSink(BlockState state);

    /** The sink for a decoration at {@code pos}, from whatever is below it. */
    static int sinkFor(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState state = level.getBlockState(below);
        if (state.getBlock() instanceof DecorSurface surface) {
            int sink = surface.decorSink(state);
            if (sink != NOT_A_SURFACE) {
                return Math.max(0, Math.min(MAX_SINK, sink));
            }
        }
        if (state.isAir() || state.getBlock() instanceof DecorBlock) {
            return 0;
        }
        VoxelShape shape = state.getCollisionShape(level, below);
        if (shape.isEmpty()) {
            return 0;
        }
        double top = shape.max(Direction.Axis.Y) * 16.0D;
        if (top >= 16.0D || top < 8.0D) {
            return 0;
        }
        return (int) Math.round((16.0D - top) / 2.0D);
    }
}
