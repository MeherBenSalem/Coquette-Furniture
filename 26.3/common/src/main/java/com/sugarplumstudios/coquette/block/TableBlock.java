package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A dining table that joins with its neighbours into one long table. */
public class TableBlock extends ConnectingBlock implements DecorSurface {
    private static final VoxelShape TOP = ShapeUtil.px(0, 13, 0, 16, 14, 16);
    private static final VoxelShape[] LEGS = {
            ShapeUtil.px(0.75, 0, 0.75, 3.25, 13, 3.25),
            ShapeUtil.px(12.75, 0, 0.75, 15.25, 13, 3.25),
            ShapeUtil.px(12.75, 0, 12.75, 15.25, 13, 15.25),
            ShapeUtil.px(0.75, 0, 12.75, 3.25, 13, 15.25)};
    private final VoxelShape[] shapes = new VoxelShape[16];

    public TableBlock(Properties properties) {
        super(properties);
        for (int mask = 0; mask < 16; mask++) {
            boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
            VoxelShape shape = TOP;
            if (!n && !w) {
                shape = Shapes.or(shape, LEGS[0]);
            }
            if (!n && !e) {
                shape = Shapes.or(shape, LEGS[1]);
            }
            if (!s && !e) {
                shape = Shapes.or(shape, LEGS[2]);
            }
            if (!s && !w) {
                shape = Shapes.or(shape, LEGS[3]);
            }
            shapes[mask] = shape.optimize();
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0) | (state.getValue(SOUTH) ? 4 : 0)
                | (state.getValue(WEST) ? 8 : 0);
        return shapes[mask];
    }

    @Override
    public int decorSink(BlockState state) {
        return 1;
    }
}
