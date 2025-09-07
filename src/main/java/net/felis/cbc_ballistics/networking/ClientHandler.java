package net.felis.cbc_ballistics.networking;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientHandler {

    public static void sendReadyCannons(BlockPos pos, int numReady) {
        Entity player = Minecraft.getInstance().player;
        Level level = player.level();
        BlockEntity blockS = level.getBlockEntity(pos);
        if (blockS instanceof ArtilleryCoordinatorBlockEntity block) {
            block.getPersistentData().putInt("readies", numReady);
        }
    }

    public static void SyncArtilleryNet(BlockPos pos, CompoundTag tags) {
        Entity player = Minecraft.getInstance().player;
        Level level = player.level();
        BlockEntity blockS = level.getBlockEntity(pos);
        if (blockS instanceof ArtilleryCoordinatorBlockEntity block) {
            block.reconnectNetwork(tags);
        }
    }

    public static void SyncCalculator(BlockPos pos, int[] targetPos) {
        Entity player = Minecraft.getInstance().player;
        Level level = player.level();
        BlockEntity blockS = level.getBlockEntity(pos);
        if (blockS instanceof CalculatorBlockEntity block) {
            block.setTarget(targetPos);
        }
    }
}
