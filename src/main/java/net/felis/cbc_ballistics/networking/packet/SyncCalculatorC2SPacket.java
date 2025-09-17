package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.felis.cbc_ballistics.entity.custom.RangefinderEntity;
import net.felis.cbc_ballistics.item.ModItems;
import net.felis.cbc_ballistics.item.custom.ArtilleryNetworkManagerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCalculatorC2SPacket {

    private BlockPos pos;
    private CompoundTag tags;

    public SyncCalculatorC2SPacket(BlockPos pos, CompoundTag tags) {
        this.pos = pos;
        this.tags = tags;
    }

    public SyncCalculatorC2SPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readNbt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(tags);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel)player.level();
            BlockEntity blockS = level.getBlockEntity(pos);
            if(blockS instanceof CalculatorBlockEntity block) {
                block.syncFrom(tags);
            }

        });
        return true;
    }
}
