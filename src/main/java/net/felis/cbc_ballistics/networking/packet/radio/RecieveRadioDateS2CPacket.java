package net.felis.cbc_ballistics.networking.packet.radio;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ClientHandler;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RecieveRadioDateS2CPacket {

    private CompoundTag tags;

    public RecieveRadioDateS2CPacket(CompoundTag tags) {
        this.tags = tags;
    }

    public RecieveRadioDateS2CPacket(FriendlyByteBuf buf) {
        this(buf.readAnySizeNbt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(tags);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientHandler.receiveRadioData(tags);
        });
        return true;
    }

}
