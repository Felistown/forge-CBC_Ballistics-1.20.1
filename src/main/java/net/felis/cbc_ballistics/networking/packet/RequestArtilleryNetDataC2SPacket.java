package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestArtilleryNetDataC2SPacket {

    private BlockPos pos;

    public RequestArtilleryNetDataC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public RequestArtilleryNetDataC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        System.out.println("requesting");
        NetworkEvent.Context context = supplier.get();
        supplier.get().enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel) player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if (blockS instanceof ArtilleryCoordinatorBlockEntity be) {
                be.safeSyncToClient(player);
            } else {
                System.out.println("nothing");
            }
        });
        return true;
    }
}
