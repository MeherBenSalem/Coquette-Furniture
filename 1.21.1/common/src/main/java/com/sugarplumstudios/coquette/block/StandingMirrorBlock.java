package com.sugarplumstudios.coquette.block;

import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A full-length cheval mirror. It always has something nice to say. */
public class StandingMirrorBlock extends TallFurnitureBlock {
    public StandingMirrorBlock(Properties properties, VoxelShape lowerNorth, VoxelShape upperNorth) {
        super(properties, lowerNorth, upperNorth, false);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel server) {
            BlockPos lower = lowerPos(state, pos);
            Pampering.compliment(server, player, DecorBlock.modelPoint(state, lower, 8, 18, 6));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
