package com.sugarplumstudios.coquette.block;

import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Wardrobes and refrigerators: the storage lives in the lower half and both halves swing open. */
public class TallStorageBlock extends TallFurnitureBlock implements EntityBlock, StorageFurniture {
    private final int slots;
    private final String title;
    private final SoundEvent openSound;
    private final SoundEvent closeSound;

    public TallStorageBlock(Properties properties, VoxelShape lowerNorth, VoxelShape upperNorth, int slots, String title,
                            SoundEvent openSound, SoundEvent closeSound) {
        super(properties, lowerNorth, upperNorth, false);
        this.slots = slots;
        this.title = title;
        this.openSound = openSound;
        this.closeSound = closeSound;
        registerDefaultState(defaultBlockState().setValue(StorageBlock.OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(StorageBlock.OPEN);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(lowerPos(state, pos)) instanceof FurnitureStorageBlockEntity storage) {
            player.openMenu(storage);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FurnitureStorageBlockEntity storage) {
            storage.recheckOpen();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        StorageBlock.dropContents(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new FurnitureStorageBlockEntity(pos, state) : null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(lowerPos(state, pos)));
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
        if (state.getValue(StorageBlock.OPEN) != open) {
            setBoth(level, pos, state, s -> s.setValue(StorageBlock.OPEN, open));
        }
    }
}
