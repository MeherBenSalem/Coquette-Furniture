package com.sugarplumstudios.coquette.block;

import net.minecraft.world.ItemInteractionResult;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Sitting on chairs, sofas, benches, pillows, swings and bathtubs. */
public final class Seats {
    private Seats() {
    }

    /** True if a click with this hand should try to sit (empty hand or a non-block item, not sneaking). */
    public static boolean wantsToSit(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSecondaryUseActive()) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);
        return !(held.getItem() instanceof BlockItem);
    }

    public static ItemInteractionResult sit(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        if (!(state.getBlock() instanceof SeatProvider seat) || !wantsToSit(player, hand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!CoquetteConfig.COMMON.seating.get()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Vec3 at = seat.seatPosition(state, pos);
        if (at == null || player.isPassenger()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (player.position().distanceToSqr(at) > 9.0D) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(at, at).inflate(0.4D)).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.coquettefurniture.seat_occupied"), true);
            return ItemInteractionResult.CONSUME;
        }
        SeatEntity entity = new SeatEntity(level, pos, at, seat.seatYaw(state));
        level.addFreshEntity(entity);
        player.setYRot(seat.seatYaw(state));
        player.setYHeadRot(seat.seatYaw(state));
        player.startRiding(entity);
        return ItemInteractionResult.CONSUME;
    }
}
