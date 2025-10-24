package net.felis.cbc_ballistics.networking;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.networking.packet.*;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.*;
import net.felis.cbc_ballistics.networking.packet.ballisticCalculator.SyncCalculatorC2SPacket;
import net.felis.cbc_ballistics.networking.packet.ballisticCalculator.SyncCalculatorS2CPacket;
import net.felis.cbc_ballistics.networking.packet.radio.RecieveRadioDateS2CPacket;
import net.felis.cbc_ballistics.networking.packet.radio.SendRadioDataC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
        INSTANCE.messageBuilder(sendSolutionsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(sendSolutionsS2CPacket::new)
                .encoder(sendSolutionsS2CPacket::toBytes)
                .consumerMainThread(sendSolutionsS2CPacket::handle)
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
        INSTANCE.messageBuilder(SendArtilleryNetworkInstructionC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SendArtilleryNetworkInstructionC2SPacket::new)
                .encoder(SendArtilleryNetworkInstructionC2SPacket::toBytes)
                .consumerMainThread(SendArtilleryNetworkInstructionC2SPacket::handle)
                .add();
        INSTANCE.messageBuilder(removeNetworkS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(removeNetworkS2CPacket::new)
                .encoder(removeNetworkS2CPacket::toBytes)
                .consumerMainThread(removeNetworkS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncArtilleryNetS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncArtilleryNetS2CPacket::new)
                .encoder(SyncArtilleryNetS2CPacket::toBytes)
                .consumerMainThread(SyncArtilleryNetS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncManagerItemS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncManagerItemS2CPacket::new)
                .encoder(SyncManagerItemS2CPacket::toBytes)
                .consumerMainThread(SyncManagerItemS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(OpenCoordinatorS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenCoordinatorS2CPacket::new)
                .encoder(OpenCoordinatorS2CPacket::toBytes)
                .consumerMainThread(OpenCoordinatorS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(UpdateArtilleryNetDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateArtilleryNetDataS2CPacket::new)
                .encoder(UpdateArtilleryNetDataS2CPacket::toBytes)
                .consumerMainThread(UpdateArtilleryNetDataS2CPacket::handle)
                .add();
        INSTANCE.messageBuilder(OpenCoordinatorC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenCoordinatorC2SPacket::new)
                .encoder(OpenCoordinatorC2SPacket::toBytes)
                .consumerMainThread(OpenCoordinatorC2SPacket::handle)
                .add();
        INSTANCE.messageBuilder(SendRadioDataC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SendRadioDataC2SPacket::new)
                .encoder(SendRadioDataC2SPacket::toBytes)
                .consumerMainThread(SendRadioDataC2SPacket::handle)
                .add();
        INSTANCE.messageBuilder(RecieveRadioDateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RecieveRadioDateS2CPacket::new)
                .encoder(RecieveRadioDateS2CPacket::toBytes)
                .consumerMainThread(RecieveRadioDateS2CPacket::handle)
                .add();
    }
    public static <MSG> void sendToServer(MSG message) {
        //System.out.println("sent to server");
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        //System.out.println("sent to player");
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToPlayersRad(MSG message, PacketDistributor.TargetPoint point) {
        //System.out.println("sent to players in radius");
        INSTANCE.send(PacketDistributor.NEAR.with(() ->  point), message);
    }

    public static <MSG> void sendToDimension(MSG message, Level level) {
        //System.out.println("sent to player in dimension " + level.dimension().toString());
        INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static <MSG> void sendToAll(MSG message) {
        //System.out.println("sent to all players");
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
