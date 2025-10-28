package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.item.custom.RangefinderItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SendRangeFinderC2SPacket {

    private int slot;
    private String results;

    public SendRangeFinderC2SPacket(ItemStack item) {
        if(item.getItem() instanceof RangefinderItem) {
            slot = Minecraft.getInstance().player.getInventory().findSlotMatchingItem(item);
            results = item.getOrCreateTag().getString("results");
        }
    }

    public SendRangeFinderC2SPacket(FriendlyByteBuf buf) {
        slot = buf.readInt();
        results = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeUtf(results);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ItemStack item = context.getSender().getInventory().getItem(slot);
            if(item.getItem() instanceof RangefinderItem) {
                item.getOrCreateTag().putString("results", results);
            }
        });
        return true;
    }

}
