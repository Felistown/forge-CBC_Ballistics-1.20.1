package net.felis.cbc_ballistics.networking.packet.radio;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendRadioDataC2SPacket {

    private String targetPos;
    private String network_id;
    private BlockPos pos;

    public SendRadioDataC2SPacket(String targetPos, String network_id, BlockPos pos) {
        this.targetPos = targetPos;
        this.network_id = network_id;
        this.pos = pos;
    }

    public SendRadioDataC2SPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(5),buf.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(targetPos);
        buf.writeUtf(network_id, 5);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel)player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if(blockS instanceof ArtilleryCoordinatorBlockEntity block && block.getNetwork_id().equals(network_id)) {
                block.setTarget(targetPos);
                ModMessages.sendToPlayer(new RecieveRadioDateS2CPacket(new Artillery_CoordinatorInterface(block).getTags()), player);
            }
        });
        return true;
    }
}
