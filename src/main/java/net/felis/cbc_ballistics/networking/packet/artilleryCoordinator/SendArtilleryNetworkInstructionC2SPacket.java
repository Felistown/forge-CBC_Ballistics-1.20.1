package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendArtilleryNetworkInstructionC2SPacket {

    private byte instruction;
    /*
     0 : set target
     1 : set
     2 : mode
     3 : fire
     4 : change id
     */
    private String value;
    private BlockPos pos;

    public SendArtilleryNetworkInstructionC2SPacket(BlockPos pos, byte instruction, String value) {
        this.pos = pos;
        this.instruction = instruction;
        this.value = value;
    }

    public SendArtilleryNetworkInstructionC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readByte(), buf.readUtf());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(instruction);
        buf.writeUtf(value);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel)player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if(blockS instanceof ArtilleryCoordinatorBlockEntity) {
                ArtilleryCoordinatorBlockEntity block = (ArtilleryCoordinatorBlockEntity) blockS;
                    switch (instruction) {
                        case 0:
                            block.setTarget(value);
                            break;
                        case 1:
                            block.target();
                            break;
                        case 2:
                            block.setMode(ArtilleryCoordinatorBlockEntity.Mode.fromByte((byte)Utils.stringToInt(value)));
                            break;
                        case 3:
                            block.fire();
                            break;
                        case 4:
                            block.setNetwork_id(value);
                            break;
                    }
            }

        });
        return true;
    }
}
