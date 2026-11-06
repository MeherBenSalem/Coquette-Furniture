package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A piece of furniture with a {@link com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity}. */
public interface StorageFurniture {
    /** Number of slots: 5 uses the hopper screen, anything else must be a multiple of 9 (up to 54). */
    int slots();

    String containerTitle();

    default SoundEvent openSound() {
        return SoundEvents.BARREL_OPEN;
    }

    default SoundEvent closeSound() {
        return SoundEvents.BARREL_CLOSE;
    }

    /** Shows the open (or closed) model while someone is looking inside. */
    void setOpen(Level level, BlockPos pos, BlockState state, boolean open);

    /** Called on the server when the first viewer opens it. */
    default void onOpened(Level level, BlockPos pos, BlockState state) {
    }
}
