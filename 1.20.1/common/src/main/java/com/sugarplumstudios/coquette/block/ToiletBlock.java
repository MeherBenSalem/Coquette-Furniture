package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The toilet: sit on it like a chair, or sneak + right-click with empty hands to flush. */
public class ToiletBlock extends ChairBlock {
    public ToiletBlock(Properties properties, VoxelShape northShape, double seatHeight, double seatDepth) {
        super(properties, northShape, seatHeight, seatDepth, 0);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (wantsToFlush(player, hand)) {
            if (!level.isClientSide) {
                flush(level, pos, state, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /** Sneaking with an empty main hand (vanilla only lets a sneaking player use a block with both hands empty). */
    public static boolean wantsToFlush(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && player.isSecondaryUseActive() && player.getItemInHand(hand).isEmpty();
    }

    private static void flush(Level level, BlockPos pos, BlockState state, Player player) {
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.9F, 0.6F);
        if (level instanceof ServerLevel server) {
            Vec3 at = DecorBlock.modelPoint(state, pos, 8, 6.5, 6.25);
            server.sendParticles(ParticleTypes.SPLASH, at.x, at.y, at.z, 16, 0.15D, 0.05D, 0.15D, 0.1D);
            server.sendParticles(ParticleTypes.BUBBLE_POP, at.x, at.y + 0.1D, at.z, 6, 0.12D, 0.02D, 0.12D, 0.01D);
        }
        player.displayClientMessage(Component.translatable("message.coquettefurniture.flush"), true);
    }
}
