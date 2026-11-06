package com.sugarplumstudios.coquette.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Right-click to pamper yourself in the mirror; sneak + right-click opens the drawer. */
public class VanityBlock extends StorageBlock {
    public VanityBlock(Properties properties, VoxelShape northShape, int surfaceSink) {
        super(properties, northShape, false, surfaceSink, 9, "container.coquettefurniture.vanity",
                SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundEvents.CHISELED_BOOKSHELF_INSERT);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            return openMenu(level, pos, player);
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Pampering.pamper(server, player, DecorBlock.modelPoint(state, pos, 8, 20, 12));
        }
        return InteractionResult.SUCCESS;
    }
}
