package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A cushioned bench swing hung on flower-wrapped ropes. */
public class SwingBlock extends TallFurnitureBlock implements SeatProvider {
    public SwingBlock(Properties properties, VoxelShape lowerNorth, VoxelShape upperNorth) {
        super(properties, lowerNorth, upperNorth, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return Seats.sit(level, pos, state, player, hand);
    }

    @Override
    public Vec3 seatPosition(BlockState state, BlockPos pos) {
        return DecorBlock.modelPoint(state, lowerPos(state, pos), 8, 8.5, 7.5);
    }

    @Override
    public float seatYaw(BlockState state) {
        return state.getValue(FACING).toYRot();
    }
}
