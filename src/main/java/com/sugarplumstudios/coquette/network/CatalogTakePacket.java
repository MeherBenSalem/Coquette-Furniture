package com.sugarplumstudios.coquette.network;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Asks the server to deliver furniture picked in the catalog (creative mode, or if the config allows it). */
public record CatalogTakePacket(List<ResourceLocation> items, boolean stack) {
    private static final int MAX_ITEMS = 32;

    public static void encode(CatalogTakePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.items.size());
        for (ResourceLocation id : packet.items) {
            buf.writeResourceLocation(id);
        }
        buf.writeBoolean(packet.stack);
    }

    public static CatalogTakePacket decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), MAX_ITEMS);
        List<ResourceLocation> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readResourceLocation());
        }
        return new CatalogTakePacket(items, buf.readBoolean());
    }

    public static void handle(CatalogTakePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        context.get().setPacketHandled(true);
        if (player == null) {
            return;
        }
        if (!player.isCreative() && !CoquetteConfig.COMMON.catalogDeliveryInSurvival.get()) {
            player.displayClientMessage(Component.translatable("message.coquettefurniture.catalog_creative_only")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        Component last = null;
        for (ResourceLocation id : packet.items) {
            if (!CoquetteFurniture.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item, packet.stack ? item.getMaxStackSize() : 1);
            last = stack.getHoverName();
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        if (last != null) {
            Component what = packet.items.size() > 1 ? Component.literal(packet.items.size() + "× ❀") : last;
            player.displayClientMessage(Component.translatable("message.coquettefurniture.catalog_given", what)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }
}
