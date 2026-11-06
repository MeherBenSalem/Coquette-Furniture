package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A little house on a post. Its bluebird chirps when you say hello, and now and then on its own. */
public class BirdhouseBlock extends FurnitureBlock {
    public BirdhouseBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, false, NOT_A_SURFACE);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Vec3 bird = DecorBlock.modelPoint(state, pos, 8, 10.5, 3.5);
            server.playSound(null, bird.x, bird.y, bird.z, SoundEvents.PARROT_AMBIENT, SoundSource.BLOCKS, 0.8F,
                    1.4F + server.getRandom().nextFloat() * 0.4F);
            for (int i = 0; i < 3; i++) {
                server.sendParticles(ParticleTypes.NOTE, bird.x, bird.y + 0.3D + i * 0.15D, bird.z, 0,
                        server.getRandom().nextDouble(), 0.0D, 0.0D, 1.0D);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (CoquetteConfig.CLIENT.birdhouseChirps.get() && level.isBrightOutside() && random.nextInt(200) == 0) {
            Vec3 bird = DecorBlock.modelPoint(state, pos, 8, 10.5, 3.5);
            level.playLocalSound(bird.x, bird.y, bird.z, SoundEvents.PARROT_AMBIENT, SoundSource.AMBIENT, 0.5F,
                    1.5F + random.nextFloat() * 0.4F, false);
            level.addParticle(ParticleTypes.NOTE, bird.x, bird.y + 0.4D, bird.z, random.nextDouble(), 0.0D, 0.0D);
        }
    }
}
