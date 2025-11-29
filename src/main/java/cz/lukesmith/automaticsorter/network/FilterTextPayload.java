package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record FilterTextPayload(BlockPos blockPos, String filterText) implements CustomPayload {
    public static final String NAME = "filter_text_change";
    public static final CustomPayload.Id<FilterTextPayload> ID = new CustomPayload.Id<>(Identifier.of(AutomaticSorter.MOD_ID, NAME));
    public static final PacketCodec<PacketByteBuf, FilterTextPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            FilterTextPayload::blockPos,
            PacketCodecs.STRING,
            FilterTextPayload::filterText,
            FilterTextPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
