package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Hang on a wall over a window; right-click to draw them open (tied back with bows) or closed. */
public class CurtainBlock extends TallFurnitureBlock {
    public CurtainBlock(Properties properties, VoxelShape lowerNorth, VoxelShape upperNorth) {
        super(properties, lowerNorth, upperNorth, true);
        registerDefaultState(defaultBlockState().setValue(StorageBlock.OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(StorageBlock.OPEN);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || player.getItemInHand(hand).getItem() instanceof BlockItem) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            boolean open = !state.getValue(StorageBlock.OPEN);
            setBoth(level, pos, state, s -> s.setValue(StorageBlock.OPEN, open));
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, open ? 1.2F : 0.9F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
