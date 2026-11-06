package com.sugarplumstudios.coquette.block.entity;

import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Cooks smoking recipes with the vanilla smoker screen. */
public class StoveBlockEntity extends AbstractFurnaceBlockEntity {
    public StoveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STOVE.get(), pos, state, RecipeType.SMOKING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.coquettefurniture.stove");
    }

    @Override
    protected int getBurnDuration(FuelValues fuelValues, ItemStack fuel) {
        return super.getBurnDuration(fuelValues, fuel) / 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SmokerMenu(id, inventory, this, this.dataAccess);
    }
}
