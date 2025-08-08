package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.simple.SimpleProtocol;


public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "main"), // novější způsob
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                FilterTypePacket.class,
                FilterTypePacket::encode,
                FilterTypePacket::decode,
                FilterTypePacket::handle
        );
    }
}

