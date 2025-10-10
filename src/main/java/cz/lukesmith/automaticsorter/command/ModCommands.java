package cz.lukesmith.automaticsorter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import cz.lukesmith.automaticsorter.network.ModConfigSyncPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import cz.lukesmith.automaticsorter.config.ModConfig;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("as_basespeed")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                            .executes(ctx -> {
                                double value = DoubleArgumentType.getDouble(ctx, "value");
                                ModConfig.get().baseSortingSpeed = value;
                                ModConfig.save();
                                ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.basespeed_set", value), true);
                                sendSyncConfigPacketToAllPlayers(ctx);

                                return Command.SINGLE_SUCCESS;
                            })));

            dispatcher.register(CommandManager.literal("as_basespeedboostperupgrade")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                            .executes(ctx -> {
                                double value = DoubleArgumentType.getDouble(ctx, "value");
                                ModConfig.get().baseSpeedBoostPerUpgrade = value;
                                ModConfig.save();
                                ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.basespeedboost_set", value), true);
                                sendSyncConfigPacketToAllPlayers(ctx);

                                return Command.SINGLE_SUCCESS;
                            })));

            dispatcher.register(CommandManager.literal("as_instantsort")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                boolean value = BoolArgumentType.getBool(ctx, "value");
                                ModConfig.get().instantSort = value;
                                ModConfig.save();
                                ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.instantsort_set", value ? "true" : "false"), true);
                                sendSyncConfigPacketToAllPlayers(ctx);

                                return Command.SINGLE_SUCCESS;
                            })));

        });
    }

    private static void sendSyncConfigPacketToAllPlayers(CommandContext<ServerCommandSource> ctx) {
        ModConfig config = ModConfig.get();
        ModConfigSyncPayload payload = new ModConfigSyncPayload(
                config.baseSortingSpeed,
                config.baseSpeedBoostPerUpgrade,
                config.instantSort
        );

        for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
