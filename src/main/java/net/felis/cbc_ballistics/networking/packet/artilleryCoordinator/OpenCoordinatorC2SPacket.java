package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenCoordinatorC2SPacket {

    private BlockPos pos;
    private String network_id;

    public OpenCoordinatorC2SPacket(BlockPos pos, String network_id) {
        this.pos = pos;
        this.network_id = network_id;
    }

    public OpenCoordinatorC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(5));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(network_id, 5);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel)player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if(blockS instanceof ArtilleryCoordinatorBlockEntity block && block.getNetwork_id().equals(network_id)) {
                ModMessages.sendToPlayer(new OpenCoordinatorS2CPacket(block, false), player);
            } else {
                ModMessages.sendToPlayer(new OpenCoordinatorS2CPacket(), player);
            }
        });
        return true;
    }
}
