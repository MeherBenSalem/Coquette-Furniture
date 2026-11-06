package com.sugarplumstudios.coquette.forge;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CoquetteFurniture.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ForgeNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(CatalogTakePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CatalogTakePacket::encode)
                .decoder(CatalogTakePacket::decode)
                .consumerMainThread((packet, context) -> {
                    CatalogTakePacket.handle(packet, context.get().getSender());
                    context.get().setPacketHandled(true);
                })
                .add();
    }
}
