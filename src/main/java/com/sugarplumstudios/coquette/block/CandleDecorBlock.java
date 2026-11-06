package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Three pastel candles on a gold dish. Light them with flint and steel, snuff them with a click. */
public class CandleDecorBlock extends DecorBlock {
    private static final double[][] WICKS = {{6, 8.5, 6.5}, {10, 6.5, 7.5}, {8, 5, 10.25}};

    public CandleDecorBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
        registerDefaultState(defaultBlockState().setValue(LampBlock.LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LampBlock.LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean lit = state.getValue(LampBlock.LIT);
        if (!lit && (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE))) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LampBlock.LIT, true), Block.UPDATE_ALL);
                level.playSound(null, pos, held.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (held.is(Items.FIRE_CHARGE)) {
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                } else {
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (lit && held.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LampBlock.LIT, false), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LampBlock.LIT)) {
            return;
        }
        for (double[] wick : WICKS) {
            Vec3 at = modelPoint(state, pos, wick[0], wick[1] + 0.5, wick[2]);
            if (random.nextFloat() < 0.25F) {
                level.addParticle(ParticleTypes.SMOKE, at.x, at.y + 0.1D, at.z, 0.0D, 0.0D, 0.0D);
            }
            if (random.nextFloat() < 0.1F) {
                level.addParticle(ParticleTypes.SMALL_FLAME, at.x, at.y, at.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
