package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

    public static InteractionResult sit(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        if (!(state.getBlock() instanceof SeatProvider seat) || !wantsToSit(player, hand)) {
            return InteractionResult.PASS;
        }
        if (!CoquetteConfig.COMMON.seating.get()) {
            return InteractionResult.PASS;
        }
        Vec3 at = seat.seatPosition(state, pos);
        if (at == null || player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.position().distanceToSqr(at) > 9.0D) {
            return InteractionResult.PASS;
        }
        if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(at, at).inflate(0.4D)).isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.coquettefurniture.seat_occupied"));
            return InteractionResult.CONSUME;
        }
        SeatEntity entity = new SeatEntity(level, pos, at, seat.seatYaw(state));
        level.addFreshEntity(entity);
        player.setYRot(seat.seatYaw(state));
        player.setYHeadRot(seat.seatYaw(state));
        player.startRiding(entity);
        return InteractionResult.CONSUME;
    }
}
