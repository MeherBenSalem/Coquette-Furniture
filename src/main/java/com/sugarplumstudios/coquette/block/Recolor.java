package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

/** Repaints placed furniture in another colourway, keeping its state and contents. */
public final class Recolor {
    private static boolean swapping;

    private Recolor() {
    }

    /** True while a recolour is replacing blocks, so containers don't spill their contents. */
    public static boolean isSwapping() {
        return swapping;
    }

    /** Returns true if the furniture at {@code pos} was (or on the client, would be) recoloured. */
    public static boolean tryRecolor(Level level, BlockPos pos, FurnitureColor color) {
        BlockState state = level.getBlockState(pos);
        FurnitureType type = ModBlocks.typeOf(state.getBlock());
        if (type == null || ModBlocks.colorOf(state.getBlock()) == color) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        Block target = ModBlocks.furniture(type, color);
        BlockPos other = partner(state, pos);
        swapping = true;
        try {
            if (other != null) {
                swap(level, other, level.getBlockState(other), target, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            swap(level, pos, state, target, Block.UPDATE_ALL);
            if (other != null) {
                level.blockUpdated(other, target);
            }
        } finally {
            swapping = false;
        }
        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    /** The other half of a two-block piece (beds, bathtubs, wardrobes...), if any. */
    @Nullable
    private static BlockPos partner(BlockState state, BlockPos pos) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        }
        if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT ? pos.relative(facing) : pos.relative(facing.getOpposite());
        }
        return null;
    }

    private static void swap(Level level, BlockPos pos, BlockState from, Block to, int flags) {
        if (from.getBlock() instanceof BedBlock && from.getValue(BedBlock.OCCUPIED)) {
            from = from.setValue(BedBlock.OCCUPIED, false);
        }
        BlockState target = copyProperties(from, to.defaultBlockState());
        BlockEntity oldEntity = level.getBlockEntity(pos);
        CompoundTag data = oldEntity == null ? null : oldEntity.saveWithoutMetadata();
        if (oldEntity != null) {
            level.removeBlockEntity(pos);
        }
        level.setBlock(pos, target, flags);
        if (data != null) {
            BlockEntity newEntity = level.getBlockEntity(pos);
            if (newEntity != null) {
                newEntity.load(data);
                newEntity.setChanged();
            }
        }
    }

    private static BlockState copyProperties(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (to.hasProperty(property)) {
                to = copy(from, to, property);
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
