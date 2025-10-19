package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.networking.ClientHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateArtilleryNetDataS2CPacket {

    private CompoundTag tags;

    public UpdateArtilleryNetDataS2CPacket(CompoundTag tags) {
        this.tags = tags;
    }

    public UpdateArtilleryNetDataS2CPacket(FriendlyByteBuf buf) {
        this(buf.readAnySizeNbt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(tags);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.updateNetworkData(tags));
        });
        return true;
    }

}
