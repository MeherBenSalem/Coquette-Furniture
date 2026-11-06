package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.ChatFormatting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A two-block clawfoot tub. Fill it with a water bucket (scoop the water back out
 * with an empty one) and sit in it for a bubble bath.
 */
public class BathtubBlock extends HorizontalDirectionalBlock implements SeatProvider {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final Map<Direction, VoxelShape> HEAD = ShapeUtil.horizontal(ShapeUtil.px(
            1, 0, 1, 15, 3.5, 16,
            0.75, 3.5, 0.75, 2.75, 13.75, 16,
            13.25, 3.5, 0.75, 15.25, 13.75, 16,
            2.5, 3.5, 0.75, 13.5, 16, 2.75));
    private static final Map<Direction, VoxelShape> FOOT = ShapeUtil.horizontal(ShapeUtil.px(
            1, 0, 0, 15, 3.5, 15,
            0.75, 3.5, 0, 2.75, 13.75, 15.25,
            13.25, 3.5, 0, 15.25, 13.75, 15.25,
            2.5, 3.5, 13.25, 13.5, 13.75, 15.25));

    private static final MapCodec<BathtubBlock> CODEC = simpleCodec(BathtubBlock::new);

    @Override
    protected MapCodec<BathtubBlock> codec() {
        return CODEC;
    }

    public BathtubBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, BedPart.FOOT).setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, FILLED);
    }

    private static Direction towardsOther(BedPart part, Direction facing) {
        return part == BedPart.FOOT ? facing : facing.getOpposite();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos head = context.getClickedPos().relative(facing);
        Level level = context.getLevel();
        if (!level.getBlockState(head).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(head)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            level.setBlock(pos.relative(state.getValue(FACING)), state.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == towardsOther(state.getValue(PART), state.getValue(FACING))) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART)
                    ? state.setValue(FILLED, neighborState.getValue(FILLED)) : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative() && state.getValue(PART) == BedPart.FOOT) {
            BlockPos other = pos.relative(towardsOther(BedPart.FOOT, state.getValue(FACING)));
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this) && otherState.getValue(PART) == BedPart.HEAD) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, other, Block.getId(otherState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean filled = state.getValue(FILLED);
        if (held.is(Items.WATER_BUCKET) && !filled) {
            if (!level.isClientSide()) {
                setFilled(level, pos, state, true);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.BUCKET) && filled) {
            if (!level.isClientSide()) {
                setFilled(level, pos, state, false);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.WATER_BUCKET)));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return Seats.sit(level, pos, state, player, hand);
    }

    private void setFilled(Level level, BlockPos pos, BlockState state, boolean filled) {
        BlockPos other = pos.relative(towardsOther(state.getValue(PART), state.getValue(FACING)));
        BlockState otherState = level.getBlockState(other);
        if (otherState.is(this)) {
            level.setBlock(other, otherState.setValue(FILLED, filled), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        level.setBlock(pos, state.setValue(FILLED, filled), Block.UPDATE_ALL);
    }

    private static BlockPos headPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(FACING));
    }

    @Override
    public Vec3 seatPosition(BlockState state, BlockPos pos) {
        return DecorBlock.modelPoint(state, headPos(state, pos), 8, 4.5, 12);
    }

    @Override
    public float seatYaw(BlockState state) {
        return state.getValue(FACING).getOpposite().toYRot();
    }

    @Override
    public void onSeatTick(Level level, BlockPos pos, BlockState state, Entity passenger, int tick) {
        if (!state.getValue(FILLED) || !(level instanceof ServerLevel server)) {
            return;
        }
        if (tick == 1 && passenger instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("message.coquettefurniture.bath_relax")
                    .withStyle(ChatFormatting.AQUA));
        }
        if (tick % 10 == 0) {
            Vec3 at = seatPosition(state, pos);
            server.sendParticles(ParticleTypes.BUBBLE_POP, at.x, at.y + 0.4D, at.z, 4, 0.35D, 0.1D, 0.5D, 0.01D);
        }
        if (tick % 40 == 0 && CoquetteConfig.COMMON.bathRegeneration.get() && passenger instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(FILLED) && random.nextInt(4) == 0) {
            Vec3 at = DecorBlock.modelPoint(state, pos, 3 + random.nextDouble() * 10, 11, 3 + random.nextDouble() * 10);
            level.addParticle(ParticleTypes.BUBBLE_POP, at.x, at.y, at.z, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(PART) == BedPart.HEAD ? HEAD : FOOT).get(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        BlockPos foot = pos.relative(state.getValue(FACING), state.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return net.minecraft.util.Mth.getSeed(foot.getX(), pos.getY(), foot.getZ());
    }
}
