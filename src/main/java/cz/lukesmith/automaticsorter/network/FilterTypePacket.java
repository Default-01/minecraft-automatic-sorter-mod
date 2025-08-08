package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FilterTypePacket {
    private final BlockPos pos;
    private final int filterType;

    public FilterTypePacket(BlockPos pos, int filterType) {
        this.pos = pos;
        this.filterType = filterType;
    }

    public static void encode(FilterTypePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.filterType);
    }

    public static FilterTypePacket decode(FriendlyByteBuf buf) {
        return new FilterTypePacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(FilterTypePacket msg, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            Level world = player.level();
            if (world.getBlockEntity(msg.pos) instanceof FilterBlockEntity fe) {
                fe.setFilterType(msg.filterType);
                fe.setChanged();
            }
        });
        ctx.setPacketHandled(true);
    }
}

