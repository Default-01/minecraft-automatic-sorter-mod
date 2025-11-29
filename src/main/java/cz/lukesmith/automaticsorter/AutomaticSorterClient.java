package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.network.FilterTextPayload;
import cz.lukesmith.automaticsorter.network.FilterTypePayload;
import cz.lukesmith.automaticsorter.network.SyncFilterTextPayload;
import cz.lukesmith.automaticsorter.screen.FilterScreen;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutomaticSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER, FilterScreen::new);

        PayloadTypeRegistry.playS2C().register(FilterTypePayload.ID, FilterTypePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FilterTextPayload.ID, FilterTextPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncFilterTextPayload.ID, SyncFilterTextPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                AutomaticSorter.LOGGER.info("Received packet");
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FilterTextPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                AutomaticSorter.LOGGER.info("Received text filter packet");
            });
        });

        // Register S2C packet handler for syncing filter text from server
        ClientPlayNetworking.registerGlobalReceiver(SyncFilterTextPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.player().getWorld().getBlockEntity(payload.blockPos()) instanceof FilterBlockEntity filterBlockEntity) {
                    filterBlockEntity.setTextFilter(payload.filterText());
                }
            });
        });
    }
}
