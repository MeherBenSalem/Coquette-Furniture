package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A glass-fronted cabinet with two shelves that shows off four items. Right-click
 * a shelf spot with an item to put it on display, with an empty hand to take it back.
 */
public class DisplayCabinetBlock extends FurnitureBlock implements EntityBlock {
    public DisplayCabinetBlock(Properties properties, VoxelShape northShape) {
        super(properties, northShape, false, 0);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DisplayCabinetBlockEntity cabinet)) {
            return InteractionResult.PASS;
        }
        double localX = hit.getLocation().x - pos.getX();
        double localZ = hit.getLocation().z - pos.getZ();
        double[] north = ShapeUtil.toNorthFrame(state.getValue(FACING), localX, localZ);
        double localY = hit.getLocation().y - pos.getY();
        int slot = (localY > 0.5D ? 2 : 0) + (north[0] > 0.5D ? 0 : 1);
        ItemStack held = player.getItemInHand(hand);
        ItemStack shown = cabinet.getItem(slot);
        if (!held.isEmpty() && shown.isEmpty()) {
            if (!level.isClientSide) {
                cabinet.setItem(slot, player.getAbilities().instabuild ? held.copyWithCount(1) : held.split(1));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (shown.isEmpty()) {
                slot = cabinet.lastFilledSlot();
                if (slot < 0) {
                    return InteractionResult.PASS;
                }
            }
            if (!level.isClientSide) {
                ItemStack taken = cabinet.removeItem(slot);
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !Recolor.isSwapping() && level.getBlockEntity(pos) instanceof DisplayCabinetBlockEntity cabinet) {
            Containers.dropContents(level, pos, cabinet.items());
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayCabinetBlockEntity(pos, state);
    }
}
