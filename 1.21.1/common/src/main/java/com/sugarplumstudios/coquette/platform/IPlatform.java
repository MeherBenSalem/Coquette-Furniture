package com.sugarplumstudios.coquette.platform;

import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** What Coquette Furniture needs from the mod loader. One implementation per loader. */
public interface IPlatform {
    String name();

    boolean isModLoaded(String modId);

    Path configDir();

    /** Registers an object under coquettefurniture:name in the given registry. */
    <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTab();

    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /** Client only: sends a catalog request to the server. */
    void sendToServer(CatalogTakePacket packet);
}
