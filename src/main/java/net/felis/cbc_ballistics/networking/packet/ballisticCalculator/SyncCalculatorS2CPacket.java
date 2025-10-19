package net.felis.cbc_ballistics.networking.packet.ballisticCalculator;

import net.felis.cbc_ballistics.networking.ClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCalculatorS2CPacket {

    private BlockPos pos;
    private int[] targetPos;

    public SyncCalculatorS2CPacket(BlockPos pos, int[] targetPos) {
        this.pos = pos;
        this.targetPos = targetPos;
    }

    public SyncCalculatorS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readVarIntArray(3));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarIntArray(targetPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.SyncCalculator(pos, targetPos);
            });
        });
        return true;
    }

}
