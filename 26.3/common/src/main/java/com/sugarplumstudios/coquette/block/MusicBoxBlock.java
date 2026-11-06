package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.block.entity.MusicBoxBlockEntity;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A heart-shaped music box with a twirling ballerina on top. Right-click to wind it up. */
public class MusicBoxBlock extends DecorBlock implements EntityBlock {
    public MusicBoxBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MusicBoxBlockEntity box && !box.melody().playing()) {
            box.melody().start();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MusicBoxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.MUSIC_BOX.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<MusicBoxBlockEntity>) (l, p, s, be) -> be.melody().tick(l, p);
    }
}
