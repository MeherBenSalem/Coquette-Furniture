package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A real bed (sleeping, respawn points and nether explosions all behave like
 * vanilla) drawn with a baked canopy model instead of vanilla's bed renderer.
 */
public class CanopyBedBlock extends BedBlock implements DecorSurface {
    private static final Map<Direction, VoxelShape> HEAD = ShapeUtil.horizontal(ShapeUtil.px(
            0, 0, 0, 16, 9.75, 16,
            0, 0, 0, 16, 16, 2.5));
    private static final Map<Direction, VoxelShape> FOOT = ShapeUtil.horizontal(ShapeUtil.px(
            0, 0, 0, 16, 9.75, 16,
            0, 0, 14, 16, 12.75, 16,
            0, 0, 14, 2, 16, 16,
            14, 0, 14, 16, 16, 16));

    private final FurnitureColor color;

    public CanopyBedBlock(FurnitureColor color, Properties properties) {
        super(color.dye(), properties);
        this.color = color;
    }

    public FurnitureColor furnitureColor() {
        return color;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(PART) == BedPart.HEAD ? HEAD : FOOT).get(state.getValue(FACING));
    }

    @Override
    public int decorSink(BlockState state) {
        return 4;
    }
}
