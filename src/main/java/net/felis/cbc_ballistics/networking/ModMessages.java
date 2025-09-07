package net.felis.cbc_ballistics.networking;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.networking.packet.*;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    public static SimpleChannel INSTANCE= NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CBC_Ballistics.MODID, "messages"))
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
        INSTANCE.messageBuilder(SyncCalculatorS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncCalculatorS2CPacket::new)
                .encoder(SyncCalculatorS2CPacket::toBytes)
                .consumerMainThread(SyncCalculatorS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(SendReadyCannonsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendReadyCannonsS2CPacket::new)
                .encoder(SendReadyCannonsS2CPacket::toBytes)
                .consumerMainThread(SendReadyCannonsS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(SendArtilleryNetworkInstructionC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SendArtilleryNetworkInstructionC2SPacket::new)
                .encoder(SendArtilleryNetworkInstructionC2SPacket::toBytes)
                .consumerMainThread(SendArtilleryNetworkInstructionC2SPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncArtilleryNetS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncArtilleryNetS2CPacket::new)
                .encoder(SyncArtilleryNetS2CPacket::toBytes)
                .consumerMainThread(SyncArtilleryNetS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToPlayersRad(MSG message, PacketDistributor.TargetPoint point) {
        INSTANCE.send(PacketDistributor.NEAR.with(() ->  point), message);
    }


}
