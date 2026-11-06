package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Up to three cushions in one pile. Sit on them on the floor, or toss them onto a bed or sofa. */
public class PillowBlock extends DecorBlock implements SeatProvider {
    public static final IntegerProperty PILLOWS = IntegerProperty.create("pillows", 1, 3);

    public PillowBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
        registerDefaultState(defaultBlockState().setValue(PILLOWS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PILLOWS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            return existing.setValue(PILLOWS, Math.min(3, existing.getValue(PILLOWS) + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(PILLOWS) < 3
                || super.canBeReplaced(state, context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return Seats.sit(level, pos, state, player, hand);
    }

    @Override
    public Vec3 seatPosition(BlockState state, BlockPos pos) {
        return DecorBlock.modelPoint(state, pos, 8, 3.5, 8);
    }

    @Override
    public float seatYaw(BlockState state) {
        return state.getValue(FACING).toYRot();
    }
}
