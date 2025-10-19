package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.networking.ClientHandler;
import net.felis.cbc_ballistics.util.calculator.FiringSolutions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class sendSolutionsS2CPacket {

    private BlockPos pos;
    private FiringSolutions solutions;

    public sendSolutionsS2CPacket(BlockPos pos, FiringSolutions solutions) {
        System.out.println("sending...");
        this.pos = pos;
        this.solutions = solutions;
    }

    public sendSolutionsS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), new FiringSolutions(buf.readAnySizeNbt()));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(solutions.toTags());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientHandler.sendSolutions(solutions, pos);
        });
        return true;
    }
}
