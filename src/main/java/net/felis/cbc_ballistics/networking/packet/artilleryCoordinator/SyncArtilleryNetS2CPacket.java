package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.networking.ClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncArtilleryNetS2CPacket {

    private BlockPos pos;
    private CompoundTag tags;

    public SyncArtilleryNetS2CPacket(BlockPos pos, CompoundTag tags) {
        this.pos = pos;
        this.tags = tags;
    }

    public SyncArtilleryNetS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readNbt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(tags);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.SyncArtilleryNet(pos, tags);
            });
        });
        return true;
    }

}
