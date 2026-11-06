package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** An oval wall mirror. Right-click for a compliment. */
public class MirrorBlock extends FurnitureBlock {
    public MirrorBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, true, NOT_A_SURFACE);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Pampering.compliment(server, player, DecorBlock.modelPoint(state, pos, 8, 8, 12));
        }
        return InteractionResult.SUCCESS;
    }
}
