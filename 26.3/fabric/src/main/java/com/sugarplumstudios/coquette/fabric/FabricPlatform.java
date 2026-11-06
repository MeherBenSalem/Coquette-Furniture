package com.sugarplumstudios.coquette.fabric;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import com.sugarplumstudios.coquette.platform.IPlatform;
import com.sugarplumstudios.coquette.platform.RegistryEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class FabricPlatform implements IPlatform {
    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        if (target == null) {
            throw new IllegalArgumentException("Unknown registry " + registry);
        }
        T value = Registry.register(target, CoquetteFurniture.id(name), factory.get());
        return new RegistryEntry<>(CoquetteFurniture.id(name), () -> value);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::apply, Set.of(blocks));
    }

    @Override
    public void sendToServer(CatalogTakePacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
