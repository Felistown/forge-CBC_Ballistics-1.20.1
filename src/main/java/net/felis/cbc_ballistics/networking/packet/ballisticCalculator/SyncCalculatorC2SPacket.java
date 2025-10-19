package net.felis.cbc_ballistics.networking.packet.ballisticCalculator;

import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCalculatorC2SPacket {

    private BlockPos pos;
    private CompoundTag tags;
    private boolean calculate;

    public SyncCalculatorC2SPacket(BlockPos pos, CompoundTag tags, boolean calculate) {
        this.pos = pos;
        this.tags = tags;
    }

    public SyncCalculatorC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readNbt(), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(tags);
        buf.writeBoolean(calculate);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel)player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if(blockS instanceof CalculatorBlockEntity block) {
                block.syncFrom(tags);
                block.calculate();
            }
        });
        return true;
    }
}
