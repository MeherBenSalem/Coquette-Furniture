package com.sugarplumstudios.coquette.fabric.mixin;

import com.sugarplumstudios.coquette.block.BookshelfBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Coquette bookshelves power enchanting tables (Forge does this through BookshelfBlock#getEnchantPowerBonus). */
@Mixin(EnchantmentTableBlock.class)
public abstract class EnchantmentTableBlockMixin {
    @Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
    private static void coquettefurniture$bookshelves(Level level, BlockPos table, BlockPos offset, CallbackInfoReturnable<Boolean> cir) {
        if (level.getBlockState(table.offset(offset)).getBlock() instanceof BookshelfBlock && BookshelfBlock.powersEnchanting()
                && level.getBlockState(table.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2))
                .is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
            cir.setReturnValue(true);
        }
    }
}
