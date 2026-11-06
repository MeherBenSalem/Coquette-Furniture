package com.sugarplumstudios.coquette.network;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Asks the server to deliver furniture picked in the catalog (creative mode, or if the config allows it). */
public record CatalogTakePacket(List<ResourceLocation> items, boolean stack) implements CustomPacketPayload {
    public static final String PROTOCOL = "1";
    public static final CustomPacketPayload.Type<CatalogTakePacket> TYPE = new CustomPacketPayload.Type<>(CoquetteFurniture.id("catalog_take"));
    public static final StreamCodec<FriendlyByteBuf, CatalogTakePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> encode(packet, buf), CatalogTakePacket::decode);
    private static final int MAX_ITEMS = 32;

    @Override
    public Type<CatalogTakePacket> type() {
        return TYPE;
    }

    public static void encode(CatalogTakePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.items.size());
        for (ResourceLocation id : packet.items) {
            buf.writeResourceLocation(id);
        }
        buf.writeBoolean(packet.stack);
    }

    public static CatalogTakePacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ITEMS) {
            throw new IllegalArgumentException("Too many catalog items: " + size);
        }
        List<ResourceLocation> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readResourceLocation());
        }
        return new CatalogTakePacket(items, buf.readBoolean());
    }

    /** Runs on the server thread. */
    public static void handle(CatalogTakePacket packet, ServerPlayer player) {
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
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item, packet.stack ? new ItemStack(item).getMaxStackSize() : 1);
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
