package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Teapot and teacups on a tray. Bring a glass bottle to pour yourself a cup of strawberry tea. */
public class TeaSetBlock extends DecorBlock {
    public TeaSetBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        Vec3 spout = modelPoint(state, pos, 11, 4, 8.5);
        if (held.is(Items.GLASS_BOTTLE) && CoquetteConfig.COMMON.teaSetPoursTea.get()) {
            if (level instanceof ServerLevel server) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(ModItems.CUP_OF_TEA.get())));
                server.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.2F);
                server.sendParticles(ParticleTypes.HEART, spout.x, spout.y + 0.2D, spout.z, 1, 0.1D, 0.1D, 0.1D, 0.0D);
                player.sendOverlayMessage(Component.translatable("message.coquettefurniture.tea_poured")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND || held.getItem() instanceof BlockItem) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            server.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 1.4F);
            server.sendParticles(ParticleTypes.CLOUD, spout.x, spout.y, spout.z, 4, 0.03D, 0.08D, 0.03D, 0.01D);
            player.sendOverlayMessage(Component.translatable("message.coquettefurniture.tea_steam")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            Vec3 lid = modelPoint(state, pos, 6.5, 6.5, 8.5);
            level.addParticle(ParticleTypes.WHITE_ASH, lid.x, lid.y, lid.z, 0.0D, 0.03D, 0.0D);
        }
    }
}
