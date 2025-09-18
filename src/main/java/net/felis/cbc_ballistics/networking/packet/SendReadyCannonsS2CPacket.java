package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.networking.ClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendReadyCannonsS2CPacket {

    private int numReady;
    private BlockPos pos;

    public SendReadyCannonsS2CPacket(BlockPos pos, int numReady) {
        this.pos = pos;
        this.numReady = numReady;
    }

    public SendReadyCannonsS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readInt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(numReady);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientHandler.sendReadyCannons(pos, numReady)
            );
        });
        return true;
    }

}
