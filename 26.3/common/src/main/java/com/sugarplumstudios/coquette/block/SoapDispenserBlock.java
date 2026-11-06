package com.sugarplumstudios.coquette.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One pump for squeaky clean hands (and it puts you out if you're on fire). */
public class SoapDispenserBlock extends DecorBlock {
    public SoapDispenserBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || player.getItemInHand(hand).getItem() instanceof BlockItem) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Vec3 nozzle = modelPoint(state, pos, 8, 7.5, 4.5);
            server.playSound(null, pos, SoundEvents.SLIME_BLOCK_STEP, SoundSource.BLOCKS, 0.7F, 1.6F);
            server.sendParticles(ParticleTypes.BUBBLE_POP, nozzle.x, nozzle.y, nozzle.z, 8, 0.15D, 0.1D, 0.15D, 0.02D);
            player.clearFire();
            player.sendOverlayMessage(Component.translatable("message.coquettefurniture.soap").withStyle(ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }
}
