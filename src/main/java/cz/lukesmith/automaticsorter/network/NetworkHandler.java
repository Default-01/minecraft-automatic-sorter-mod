package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;


public class NetworkHandler {
    private static final String CHANNEL_NAME = "main";
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.tryBuild(AutomaticSorter.MOD_ID, CHANNEL_NAME))
            .simpleChannel();

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(FilterTypePacket.class, packetId++)
                .encoder(FilterTypePacket::encode)
                .decoder(FilterTypePacket::decode)
                .consumer(FilterTypePacket::handle)
                .add();
    }
}



