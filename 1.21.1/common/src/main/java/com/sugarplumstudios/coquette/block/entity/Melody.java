package com.sugarplumstudios.coquette.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/** A little music-box tune (the opening of Für Elise) played on chimes, one note every few ticks. */
public final class Melody {
    /** Note block pitches (0 = F#3 ... 24 = F#5); -1 is a rest. */
    private static final int[] NOTES = {22, 21, 22, 21, 22, 17, 20, 18, 15, -1, 6, 10, 15, 17, -1, 10, 14, 17, 18};
    private static final int TICKS_PER_NOTE = 4;

    private int tick = -1;

    public void start() {
        tick = 0;
    }

    public boolean playing() {
        return tick >= 0;
    }

    public void tick(Level level, BlockPos pos) {
        if (tick < 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        if (tick % TICKS_PER_NOTE == 0) {
            int index = tick / TICKS_PER_NOTE;
            if (index >= NOTES.length) {
                tick = -1;
                return;
            }
            int note = NOTES[index];
            if (note >= 0) {
                float pitch = (float) Math.pow(2.0D, (note - 12) / 12.0D);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.RECORDS, 0.55F, pitch);
                server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, 0,
                        note / 24.0D, 0.0D, 0.0D, 1.0D);
            }
        }
        tick++;
    }
}
