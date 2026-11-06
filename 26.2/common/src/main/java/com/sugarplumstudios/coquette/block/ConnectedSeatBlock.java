package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/**
 * Sofas and garden benches: placed side by side (facing the same way) they join
 * into one long piece, keeping armrests only at the two ends.
 */
public class ConnectedSeatBlock extends ChairBlock {
    public static final EnumProperty<SeatShape> SHAPE = EnumProperty.create("shape", SeatShape.class);

    public ConnectedSeatBlock(Properties properties, VoxelShape northShape, double seatHeight, double seatDepth, int surfaceSink) {
        super(properties, northShape, seatHeight, seatDepth, surfaceSink);
        registerDefaultState(defaultBlockState().setValue(SHAPE, SeatShape.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(SHAPE, shapeAt(context.getLevel(), context.getClickedPos(), state));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        if (direction.getAxis().isHorizontal()) {
            updated = updated.setValue(SHAPE, shapeAt(level, pos, updated));
        }
        return updated;
    }

    private SeatShape shapeAt(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction sitterLeft = facing.getCounterClockWise();
        boolean left = joins(level.getBlockState(pos.relative(sitterLeft)), facing);
        boolean right = joins(level.getBlockState(pos.relative(sitterLeft.getOpposite())), facing);
        if (left && right) {
            return SeatShape.MIDDLE;
        }
        if (left) {
            return SeatShape.RIGHT;
        }
        return right ? SeatShape.LEFT : SeatShape.SINGLE;
    }

    private boolean joins(BlockState neighbor, Direction facing) {
        return neighbor.getBlock() == this && neighbor.getValue(FACING) == facing;
    }

    public enum SeatShape implements StringRepresentable {
        SINGLE, LEFT, MIDDLE, RIGHT;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
