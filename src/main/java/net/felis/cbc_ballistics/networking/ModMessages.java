package net.felis.cbc_ballistics.networking;

import net.felis.cbc_ballistics.CBS_Ballistics;
import net.felis.cbc_ballistics.networking.packet.RangefindC2SPacket;
import net.felis.cbc_ballistics.networking.packet.SyncCalculatorC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE= NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CBS_Ballistics.MODID, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }


    public static void register() {
        INSTANCE.messageBuilder(RangefindC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RangefindC2SPacket::new)
                .encoder(RangefindC2SPacket::toBytes)
                .consumerMainThread(RangefindC2SPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncCalculatorC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SyncCalculatorC2SPacket::new)
                .encoder(SyncCalculatorC2SPacket::toBytes)
                .consumerMainThread(SyncCalculatorC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
