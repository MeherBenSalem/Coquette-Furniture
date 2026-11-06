package com.sugarplumstudios.coquette.event;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.Recolor;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Game events shared by every loader; each loader's event hooks call these. */
public final class CommonEvents {
    /** Entity tag marking players that already got their first catalog (stored with the player, on every loader). */
    public static final String RECEIVED_CATALOG = "coquettefurniture_received_catalog";

    private CommonEvents() {
    }

    /**
     * Right-click placed furniture with a dye to repaint it.
     *
     * @return the result to cancel the click with, or null to let it continue normally
     */
    @Nullable
    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockPos pos) {
        ItemStack held = player.getItemInHand(hand);
        DyeColor dye = held.getItem() instanceof DyeItem ? held.get(DataComponents.DYE) : null;
        if (dye == null || !CoquetteConfig.COMMON.dyeRecoloring.get()) {
            return null;
        }
        if (player.isSecondaryUseActive() || !player.mayBuild() || player.isSpectator()) {
            return null;
        }
        FurnitureColor color = FurnitureColor.fromDye(dye);
        if (color == null || !Recolor.tryRecolor(level, pos, color)) {
            return null;
        }
        if (!level.isClientSide() && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static void onLogin(ServerPlayer player) {
        if (CoquetteConfig.COMMON.unlockAllRecipes.get()) {
            List<RecipeHolder<?>> ours = player.level().getServer().getRecipeManager().getRecipes().stream()
                    .filter(recipe -> CoquetteFurniture.MOD_ID.equals(recipe.id().identifier().getNamespace()))
                    .toList();
            player.awardRecipes(ours);
        }
        if (CoquetteConfig.COMMON.giveCatalogOnFirstJoin.get() && !player.getTags().contains(RECEIVED_CATALOG)) {
            player.addTag(RECEIVED_CATALOG);
            ItemStack catalog = new ItemStack(ModItems.INTERIOR_DESIGN_CATALOG.get());
            if (!player.getInventory().add(catalog)) {
                player.drop(catalog, false);
            }
        }
    }
}
