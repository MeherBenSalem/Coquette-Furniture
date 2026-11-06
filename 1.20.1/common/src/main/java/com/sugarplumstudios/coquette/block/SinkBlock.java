package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Kitchen and bathroom sinks. Turn the tap on and off with an empty hand; fill
 * buckets and bottles from it (endless water, configurable).
 */
public class SinkBlock extends FurnitureBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    private final double[] basin;

    /** @param basin the water surface centre in north-frame pixels {x, y, z} */
    public SinkBlock(Properties properties, VoxelShape northShape, int surfaceSink, double... basin) {
        super(properties, northShape, false, surfaceSink);
        this.basin = basin;
        registerDefaultState(defaultBlockState().setValue(RUNNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNNING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean endless = CoquetteConfig.COMMON.sinksGiveWater.get();
        if (endless && held.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.WATER_BUCKET)));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (endless && held.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide) {
                ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, water));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (hand != InteractionHand.MAIN_HAND || held.getItem() instanceof BlockItem) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            boolean running = !state.getValue(RUNNING);
            level.setBlock(pos, state.setValue(RUNNING, running), Block.UPDATE_ALL);
            level.playSound(null, pos, running ? SoundEvents.BUCKET_EMPTY : SoundEvents.LEVER_CLICK, SoundSource.BLOCKS,
                    0.5F, running ? 1.4F : 1.6F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(RUNNING)) {
            return;
        }
        Vec3 at = DecorBlock.modelPoint(state, pos, basin[0], basin[1], basin[2]);
        level.addParticle(ParticleTypes.SPLASH, at.x + (random.nextDouble() - 0.5D) * 0.2D, at.y, at.z + (random.nextDouble() - 0.5D) * 0.2D,
                0.0D, 0.05D, 0.0D);
        if (random.nextInt(10) == 0) {
            level.playLocalSound(at.x, at.y, at.z, SoundEvents.WEATHER_RAIN, SoundSource.BLOCKS, 0.08F, 1.6F, false);
        }
    }
}
