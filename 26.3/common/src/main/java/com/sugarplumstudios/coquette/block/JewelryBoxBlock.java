package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Five slots for treasures; the lid opens on a mirror and a tiny ballerina while it plays a tune. */
public class JewelryBoxBlock extends DecorBlock implements EntityBlock, StorageFurniture {
    public JewelryBoxBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
        registerDefaultState(defaultBlockState().setValue(StorageBlock.OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(StorageBlock.OPEN);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnitureStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.FURNITURE_STORAGE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<FurnitureStorageBlockEntity>) FurnitureStorageBlockEntity::serverTick;
    }

    @Override
    public int slots() {
        return 5;
    }

    @Override
    public String containerTitle() {
        return "container.coquettefurniture.jewelry_box";
    }

    @Override
    public SoundEvent openSound() {
        return SoundEvents.CHEST_OPEN;
    }

    @Override
    public SoundEvent closeSound() {
        return SoundEvents.CHEST_CLOSE;
    }

    @Override
    public void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.getValue(StorageBlock.OPEN) != open) {
            level.setBlock(pos, state.setValue(StorageBlock.OPEN, open), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onOpened(Level level, BlockPos pos, BlockState state) {
        if (CoquetteConfig.COMMON.musicBoxMelodies.get() && level.getBlockEntity(pos) instanceof FurnitureStorageBlockEntity storage) {
            storage.melody().start();
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
