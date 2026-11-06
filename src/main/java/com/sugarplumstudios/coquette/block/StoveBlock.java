package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.block.entity.StoveBlockEntity;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** A retro stove that cooks food twice as fast, exactly like a smoker. */
public class StoveBlock extends AbstractFurnaceBlock {
    private static final Map<Direction, VoxelShape> SHAPES = ShapeUtil.horizontal(ShapeUtil.px(
            0.5, 0, 1, 15.5, 15.75, 15.5,
            0.5, 15, 13.5, 15.5, 20.5, 15.5));

    public StoveBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof StoveBlockEntity stove) {
            player.openMenu(stove);
            player.awardStat(Stats.INTERACT_WITH_SMOKER);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, ModBlockEntities.STOVE.get());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (Recolor.isSwapping()) {
            return;
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (random.nextDouble() < 0.1D) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.SMOKER_SMOKE,
                    SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        double[][] burners = {{4.25, 4.5}, {11.75, 4.5}, {4.25, 10.25}, {11.75, 10.25}};
        double[] burner = burners[random.nextInt(burners.length)];
        Vec3 at = DecorBlock.modelPoint(state, pos, burner[0], 16, burner[1]);
        level.addParticle(ParticleTypes.SMOKE, at.x, at.y, at.z, 0.0D, 0.02D, 0.0D);
    }
}
