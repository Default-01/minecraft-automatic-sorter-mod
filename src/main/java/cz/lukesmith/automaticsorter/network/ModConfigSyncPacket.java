package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.config.ModConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Synchronizuje config ze serveru na klienta.
 */
public class ModConfigSyncPacket {
    private final double baseSortingSpeed;
    private final double baseSpeedBoostPerUpgrade;
    private final boolean instantSort;


    public ModConfigSyncPacket(double baseSortingSpeed, double baseSpeedBoostPerUpgrade, boolean instantSort) {
        this.baseSortingSpeed = baseSortingSpeed;
        this.baseSpeedBoostPerUpgrade = baseSpeedBoostPerUpgrade;
        this.instantSort = instantSort;
    }

    public static void encode(ModConfigSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.baseSortingSpeed);
        buf.writeDouble(msg.baseSpeedBoostPerUpgrade);
        buf.writeBoolean(msg.instantSort);
    }

    public static ModConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new ModConfigSyncPacket(buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    public static void handle(ModConfigSyncPacket msg, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ModConfig modConfig = ModConfig.get();
            modConfig.baseSortingSpeed = msg.baseSortingSpeed;
            modConfig.baseSpeedBoostPerUpgrade = msg.baseSpeedBoostPerUpgrade;
            modConfig.instantSort = msg.instantSort;
            ModConfig.save();
        });
        context.setPacketHandled(true);
    }
}



/*
public record ModConfigSyncPacket(double baseSortingSpeed, double baseSpeedBoostPerUpgrade, boolean instantSort)
        implements CustomPayload {

    public static final String NAME = "config_sync";
    public static final CustomPayload.Id<ModConfigSyncPacket> ID =
            new CustomPayload.Id<>(Identifier.of(AutomaticSorter.MOD_ID, NAME));

    public static final PacketCodec<PacketByteBuf, ModConfigSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ModConfigSyncPacket::baseSortingSpeed,
            PacketCodecs.DOUBLE, ModConfigSyncPacket::baseSpeedBoostPerUpgrade,
            PacketCodecs.BOOLEAN, ModConfigSyncPacket::instantSort,
            ModConfigSyncPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void applyClient() {
        ModConfig config = ModConfig.get();
        config.baseSortingSpeed = this.baseSortingSpeed;
        config.baseSpeedBoostPerUpgrade = this.baseSpeedBoostPerUpgrade;
        config.instantSort = this.instantSort;
    }
}
*/