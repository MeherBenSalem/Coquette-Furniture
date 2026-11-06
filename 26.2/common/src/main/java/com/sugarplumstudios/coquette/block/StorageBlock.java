package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bedside tables, dressers, kitchen and bathroom cabinets, counters and the vanity drawer. */
public class StorageBlock extends FurnitureBlock implements EntityBlock, StorageFurniture {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private final int slots;
    private final String title;
    private final SoundEvent openSound;
    private final SoundEvent closeSound;

    public StorageBlock(Properties properties, VoxelShape northShape, boolean wallMounted, int surfaceSink, int slots,
                        String title, SoundEvent openSound, SoundEvent closeSound) {
        super(properties, northShape, wallMounted, surfaceSink);
        this.slots = slots;
        this.title = title;
        this.openSound = openSound;
        this.closeSound = closeSound;
        registerDefaultState(defaultBlockState().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return openMenu(level, pos, player);
    }

    protected InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof FurnitureStorageBlockEntity storage) {
            player.openMenu(storage);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FurnitureStorageBlockEntity storage) {
            storage.recheckOpen();
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        // the block entity drops its contents itself (BlockEntity#preRemoveSideEffects)
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }


    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnitureStorageBlockEntity(pos, state);
    }

    @Override
    public int slots() {
        return slots;
    }

    @Override
    public String containerTitle() {
        return title;
    }

    @Override
    public SoundEvent openSound() {
        return openSound;
    }

    @Override
    public SoundEvent closeSound() {
        return closeSound;
    }

    @Override
    public void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.hasProperty(OPEN) && state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        }
    }
}
