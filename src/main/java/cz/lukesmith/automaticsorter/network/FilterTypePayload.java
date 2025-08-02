package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.core.BlockPos;

public record FilterTypePayload(BlockPos blockPos, int filterType) implements CustomPayload {
    public static final String NAME = "filter_type_change";
    public static final CustomPayload.Id<FilterTypePayload> ID = new CustomPayload.Id<>(Identifier.of(AutomaticSorter.MOD_ID, NAME));
    public static final PacketCodec<PacketByteBuf, FilterTypePayload> CODEC = PacketCodec.tuple(
            BlockPos.CODEC,
            FilterTypePayload::blockPos,
            PacketC.INTEGER,
            FilterTypePayload::filterType,
            FilterTypePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}