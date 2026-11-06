package com.sugarplumstudios.coquette.network;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CoquetteFurniture.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(CatalogTakePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CatalogTakePacket::encode)
                .decoder(CatalogTakePacket::decode)
                .consumerMainThread(CatalogTakePacket::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
