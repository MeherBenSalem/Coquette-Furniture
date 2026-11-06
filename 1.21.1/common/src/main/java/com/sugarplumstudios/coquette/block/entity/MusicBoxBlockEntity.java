package com.sugarplumstudios.coquette.block.entity;

import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps track of where the music box is in its tune. */
public class MusicBoxBlockEntity extends BlockEntity {
    private final Melody melody = new Melody();

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MUSIC_BOX.get(), pos, state);
    }

    public Melody melody() {
        return melody;
    }
}
