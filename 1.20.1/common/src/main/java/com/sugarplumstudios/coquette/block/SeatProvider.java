package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** A block players can sit on. */
public interface SeatProvider {
    /** Where the sitter's hips go, or null if this state can't be sat on. */
    @Nullable
    Vec3 seatPosition(BlockState state, BlockPos pos);

    /** The way the sitter faces (degrees, as {@link net.minecraft.core.Direction#toYRot()}). */
    float seatYaw(BlockState state);

    /** Called every tick on the server while someone sits here. */
    default void onSeatTick(Level level, BlockPos pos, BlockState state, Entity passenger, int tick) {
    }
}
