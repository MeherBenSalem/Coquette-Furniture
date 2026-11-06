package com.sugarplumstudios.coquette.event;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.Recolor;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CoquetteFurniture.MOD_ID)
public final class CommonEvents {
    private static final String RECEIVED_CATALOG = "coquettefurniture_received_catalog";

    private CommonEvents() {
    }

    /** Right-click placed furniture with a dye to repaint it. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof DyeItem dye) || !CoquetteConfig.COMMON.dyeRecoloring.get()) {
            return;
        }
        Player player = event.getEntity();
        if (player.isSecondaryUseActive() || !player.mayBuild()) {
            return;
        }
        FurnitureColor color = FurnitureColor.fromDye(dye.getDyeColor());
        if (color == null || !Recolor.tryRecolor(event.getLevel(), event.getPos(), color)) {
            return;
        }
        if (!event.getLevel().isClientSide && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (CoquetteConfig.COMMON.unlockAllRecipes.get()) {
            List<Recipe<?>> ours = player.server.getRecipeManager().getRecipes().stream()
                    .filter(recipe -> CoquetteFurniture.MOD_ID.equals(recipe.getId().getNamespace()))
                    .toList();
            player.awardRecipes(ours);
        }
        if (CoquetteConfig.COMMON.giveCatalogOnFirstJoin.get()) {
            CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            if (!persistent.getBoolean(RECEIVED_CATALOG)) {
                persistent.putBoolean(RECEIVED_CATALOG, true);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
                ItemStack catalog = new ItemStack(ModItems.INTERIOR_DESIGN_CATALOG.get());
                if (!player.getInventory().add(catalog)) {
                    player.drop(catalog, false);
                }
            }
        }
    }
}
