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

    // new 1.4.0

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FilterTypePayload.ID, FilterTypePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FilterTypePayload.ID, FilterTypePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModConfigSyncPayload.ID, ModConfigSyncPayload.CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {});
        ClientPlayNetworking.registerGlobalReceiver(ModConfigSyncPayload.ID, (payload, context) -> {
            payload.applyClient();
        });
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {
            BlockPos blockPos = payload.blockPos();
            int filterType = payload.filterType();
            context.server().execute(() -> {
                if (context.player().getWorld().getBlockEntity(blockPos) instanceof FilterBlockEntity filterBlockEntity) {
                    filterBlockEntity.setFilterType(filterType);
                    filterBlockEntity.markDirty();
                }
            });
        });
    }

    public static void sendWhenJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            NetworkHandler.sendConfigTo(handler.player);
        });
    }

    public static void sendConfigTo(ServerPlayerEntity player) {
        var config = ModConfig.get();
        var payload = new ModConfigSyncPayload(
                config.baseSortingSpeed,
                config.baseSpeedBoostPerUpgrade,
                config.instantSort
        );
        ServerPlayNetworking.send(player, payload);
    }
}



