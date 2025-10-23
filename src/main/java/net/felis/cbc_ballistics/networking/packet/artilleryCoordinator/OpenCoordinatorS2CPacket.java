package net.felis.cbc_ballistics.networking.packet.artilleryCoordinator;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.event.ModClientEvents;
import net.felis.cbc_ballistics.networking.ClientHandler;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenCoordinatorS2CPacket {

    private BlockPos pos;
    private Artillery_CoordinatorInterface data;

    public OpenCoordinatorS2CPacket(ArtilleryCoordinatorBlockEntity be, boolean allowIdChange) {
        this.pos = be.getBlockPos();
        data = new Artillery_CoordinatorInterface(be);
        data.allowIdChange(allowIdChange);
    }

    public OpenCoordinatorS2CPacket() {
        pos = new BlockPos(0,0,0);
        CompoundTag tags = new CompoundTag();
        tags.putBoolean("empty", true);
        data = new Artillery_CoordinatorInterface(tags);
    }

    private OpenCoordinatorS2CPacket(BlockPos pos, CompoundTag tags) {
        this.pos = pos;
        data = new Artillery_CoordinatorInterface(tags);
    }

    public OpenCoordinatorS2CPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readAnySizeNbt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(data.getTags());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if(data.getTags().contains("empty")) {
                    ModClientEvents.SCREEN.add(Component.translatable("item.cbc_ballistics.radio_item.empty"), 6000);
                } else {
                    ClientHandler.openCoordinator(pos, data);
                }
            });
        });
        return true;
    }

}
