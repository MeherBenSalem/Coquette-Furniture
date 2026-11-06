package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Armchairs and dining chairs. */
public class ChairBlock extends FurnitureBlock implements SeatProvider {
    private final double seatHeight;
    private final double seatDepth;

    /**
     * @param seatHeight top of the cushion, in pixels
     * @param seatDepth  where along the seat (z, in pixels from the front) the sitter's hips go
     */
    public ChairBlock(Properties properties, VoxelShape northShape, double seatHeight, double seatDepth, int surfaceSink) {
        super(properties, northShape, false, surfaceSink);
        this.seatHeight = seatHeight;
        this.seatDepth = seatDepth;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return Seats.sit(level, pos, state, player, hand);
    }

    @Override
    public Vec3 seatPosition(BlockState state, BlockPos pos) {
        return DecorBlock.modelPoint(state, pos, 8, seatHeight, seatDepth);
    }

    @Override
    public float seatYaw(BlockState state) {
        return state.getValue(FACING).toYRot();
    }
}
