package com.sugarplumstudios.coquette.block.entity;

import com.sugarplumstudios.coquette.block.StorageFurniture;
import com.sugarplumstudios.coquette.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Drawers, cabinets, wardrobes, fridges and jewelry boxes. The size comes from the block. */
public class FurnitureStorageBlockEntity extends RandomizableContainerBlockEntity {
    private static final MenuType<?>[] CHEST_MENUS = {MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6};

    private NonNullList<ItemStack> items;
    private final Melody melody = new Melody();
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(state, true);
            if (state.getBlock() instanceof StorageFurniture furniture) {
                furniture.setOpen(level, pos, state, true);
                furniture.onOpened(level, pos, state);
            }
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(state, false);
            if (state.getBlock() instanceof StorageFurniture furniture) {
                furniture.setOpen(level, pos, state, false);
            }
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            AbstractContainerMenu menu = player.containerMenu;
            return !menu.slots.isEmpty() && menu.slots.get(0).container == FurnitureStorageBlockEntity.this;
        }
    };

    public FurnitureStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FURNITURE_STORAGE.get(), pos, state);
        this.items = NonNullList.withSize(slotsFor(state), ItemStack.EMPTY);
    }

    private static int slotsFor(BlockState state) {
        return state.getBlock() instanceof StorageFurniture furniture ? furniture.slots() : 27;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        String key = getBlockState().getBlock() instanceof StorageFurniture furniture ? furniture.containerTitle() : "container.coquettefurniture.drawers";
        return Component.translatable(key);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        int size = getContainerSize();
        if (size == 5) {
            return new HopperMenu(id, inventory, this);
        }
        int rows = Math.max(1, Math.min(6, size / 9));
        return new ChestMenu(CHEST_MENUS[rows - 1], id, inventory, this, rows);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, items);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items);
        }
    }

    @Override
    public void startOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.incrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openers.decrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openers.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }

    public Melody melody() {
        return melody;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FurnitureStorageBlockEntity be) {
        be.melody.tick(level, pos);
    }

    private void playSound(BlockState state, boolean open) {
        if (level == null || !(state.getBlock() instanceof StorageFurniture furniture)) {
            return;
        }
        SoundEvent sound = open ? furniture.openSound() : furniture.closeSound();
        level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 0.6F, level.random.nextFloat() * 0.1F + 0.95F);
    }
}
