package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SyncFilterTextPayload(BlockPos blockPos, String filterText) implements CustomPayload {
    public static final String NAME = "sync_filter_text";
    public static final CustomPayload.Id<SyncFilterTextPayload> ID = new CustomPayload.Id<>(Identifier.of(AutomaticSorter.MOD_ID, NAME));
    public static final PacketCodec<PacketByteBuf, SyncFilterTextPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            SyncFilterTextPayload::blockPos,
            PacketCodecs.STRING,
            SyncFilterTextPayload::filterText,
            SyncFilterTextPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
