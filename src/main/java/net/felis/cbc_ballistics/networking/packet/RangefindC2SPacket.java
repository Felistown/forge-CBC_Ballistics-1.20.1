package net.felis.cbc_ballistics.networking.packet;

import net.felis.cbc_ballistics.entity.custom.RangefinderEntity;
import net.felis.cbc_ballistics.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RangefindC2SPacket {

    public RangefindC2SPacket() {

    }

    public RangefindC2SPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = (ServerLevel) player.level();
            ItemStack thing = player.getUseItem();
            if (thing.getItem() == ModItems.RANGEFINDER.get() && thing != null) {
                thing.setTag(new CompoundTag());
                thing.getTag().putString("results", "Rangefinding");
                RangefinderEntity ray = new RangefinderEntity(level, thing.getTag());
                float pX = player.getXRot();
                float pY = player.getYRot();
                float f = -Mth.sin(pY * 0.017453292F) * Mth.cos(pX * 0.017453292F);
                float f1 = -Mth.sin(pX * 0.017453292F);
                float f2 = Mth.cos(pY * 0.017453292F) * Mth.cos(pX * 0.017453292F);
                ray.shoot(f, f1, f2, 10f, 0.0f);
                ray.setPos(player.getX(), player.getEyeY() - Float.MIN_VALUE, player.getZ());
                ray.setOwner(player);
                level.addFreshEntity(ray);
            }
        });
        return true;
    }
}
