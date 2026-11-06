package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * A small decoration (lamp, tea set, candles, vase...) that settles onto whatever
 * it is placed on: on a coffee table it sinks 8 pixels so it stands on the table
 * top instead of floating above it.
 */
public class DecorBlock extends FurnitureBlock {
    public static final IntegerProperty SINK = IntegerProperty.create("sink", 0, DecorSurface.MAX_SINK);

    /** Outlines keep at least this much height inside their own block, so they stay easy to click. */
    private static final double MIN_OUTLINE_TOP = 4 / 16.0D;

    private final Map<Direction, VoxelShape[]> sunkShapes = new EnumMap<>(Direction.class);
    private final Map<Direction, VoxelShape[]> outlines = new EnumMap<>(Direction.class);

    public DecorBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, false, NOT_A_SURFACE);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape[] bySink = new VoxelShape[DecorSurface.MAX_SINK + 1];
            VoxelShape[] outline = new VoxelShape[DecorSurface.MAX_SINK + 1];
            for (int sink = 0; sink <= DecorSurface.MAX_SINK; sink++) {
                VoxelShape shape = ShapeUtil.rotate(northShape.move(0, -sink * 2 / 16.0D, 0), direction);
                bySink[sink] = shape;
                // a decoration sunk onto a coffee table sits mostly in the block below, which the
                // crosshair only reaches through this block; keep a sliver of it here
                if (shape.max(Direction.Axis.Y) < MIN_OUTLINE_TOP) {
                    AABB box = shape.bounds();
                    shape = Shapes.or(shape, Shapes.box(box.minX, Math.max(0, box.minY), box.minZ, box.maxX, MIN_OUTLINE_TOP, box.maxZ));
                }
                outline[sink] = shape;
            }
            sunkShapes.put(direction, bySink);
            outlines.put(direction, outline);
        }
        registerDefaultState(defaultBlockState().setValue(SINK, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SINK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(SINK, DecorSurface.sinkFor(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        if (direction == Direction.DOWN && updated.getBlock() == this) {
            updated = updated.setValue(SINK, DecorSurface.sinkFor(level, pos));
        }
        return updated;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outlines.get(state.getValue(FACING))[state.getValue(SINK)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasCollision ? sunkShapes.get(state.getValue(FACING))[state.getValue(SINK)] : Shapes.empty();
    }

    /** Height in blocks that the model was lowered by. */
    public static double sinkOffset(BlockState state) {
        return state.hasProperty(SINK) ? state.getValue(SINK) * 2 / 16.0D : 0.0D;
    }

    /** A point given in north-frame model pixels, turned into a world position for this state. */
    public static Vec3 modelPoint(BlockState state, BlockPos pos, double x, double y, double z) {
        double[] world = ShapeUtil.toWorld(state.getValue(FACING), x / 16.0D, z / 16.0D);
        return new Vec3(pos.getX() + world[0], pos.getY() + y / 16.0D - sinkOffset(state), pos.getZ() + world[1]);
    }
}
