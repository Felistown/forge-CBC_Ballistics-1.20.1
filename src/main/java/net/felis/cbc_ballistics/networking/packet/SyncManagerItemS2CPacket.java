package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ClientHandler;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncManagerItemS2CPacket {

    private int[] data;
    /*
    0 = has network
    1 = net x
    2 = net y
    3 = net z
    4 = has selected
    5 = dir x
    6 = dir y
    7 = dir z
     */

    public SyncManagerItemS2CPacket(ArtilleryCoordinatorBlockEntity net, Director sel) {
        data = new int[8];
        if(net != null) {
            data[0] = 1;
            int[] pos = Utils.blockPosToArray(net.getBlockPos());
            data[1] = pos[0];
            data[2] = pos[1];
            data[3] = pos[2];
        }
        if(sel != null) {
            data[4] = 1;
            int[] pos = Utils.blockPosToArray(sel.getBlockEntity().getBlockPos());
            data[5] = pos[0];
            data[6] = pos[1];
            data[7] = pos[2];
        }
    }

    private SyncManagerItemS2CPacket(int[] data) {
        this.data = data;
    }

    public SyncManagerItemS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarIntArray(8));
    }

    public void toBytes(FriendlyByteBuf buf) {
       buf.writeVarIntArray(data);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.SyncManagerItem(data);
            });
        });
        return true;
    }

}
