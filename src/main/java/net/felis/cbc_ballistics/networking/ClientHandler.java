package net.felis.cbc_ballistics.networking;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.felis.cbc_ballistics.item.custom.ArtilleryNetworkManagerItem;
import net.felis.cbc_ballistics.item.custom.RadioItem;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.screen.ClientHooks;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.felis.cbc_ballistics.util.calculator.FiringSolutions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

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
        Player player = Minecraft.getInstance().player;
        Level level = player.level();
        BlockEntity blockS = level.getBlockEntity(pos);
        if (blockS instanceof ArtilleryCoordinatorBlockEntity block) {
            block.reconnectNetwork(tags);
        }
    }

    public static void RemoveNetwork(BlockPos pos) {
        Entity player = Minecraft.getInstance().player;
        Level level = player.level();
        BlockEntity blockS = level.getBlockEntity(pos);
        if (blockS instanceof Director be) {
            be.removeNetwork();
        } else if(blockS instanceof Layer be) {
            be.removeNetwork();
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

    public static void SyncManagerItem(int[] data) {
        Player player = Minecraft.getInstance().player;
        Level level = player.level();
        Item item = player.getMainHandItem().getItem();
        if(item instanceof ArtilleryNetworkManagerItem i) {
            if(data[0] >= 1) {
                BlockPos pos = new BlockPos(data[1], data[2], data[3]);
                if(level.getBlockEntity(pos) instanceof ArtilleryCoordinatorBlockEntity n) {
                    i.setNetwork(n);
                } else {
                    i.setNetwork(null);
                }
            } else {
                i.setNetwork(null);
            }
            if(data[4] >= 1) {
                BlockPos pos = new BlockPos(data[5], data[6], data[7]);
                if(level.getBlockEntity(pos) instanceof Director d) {
                    i.setSelected(d);
                } else {
                    i.setSelected(null);
                }
            } else {
                i.setSelected(null);
            }
        }
    }

    public static void sendSolutions(FiringSolutions solutions, BlockPos pos) {
        try {
            Level level = Minecraft.getInstance().level;
            System.out.println("recieved");
            if (level.getBlockEntity(pos) instanceof CalculatorBlockEntity be) {
                System.out.println("setSoltuion");
                be.setSolutions(solutions);
            }
        } catch (Exception e) {
            System.out.println("-----------");
            System.out.println(e.toString());
        }
    }

    public static void openCoordinator(BlockPos pos, Artillery_CoordinatorInterface data) {
        ClientHooks.openArtilleryCoordinatorScreen(pos, data);
        Artillery_CoordinatorInterface.CLIENT_DATA = data;
    }

    public static void updateNetworkData(CompoundTag tags) {
        for(String key: tags.getAllKeys()) {
            System.out.println("net data: " + key + " of " + tags.get(key).toString());
        }
        Artillery_CoordinatorInterface.CLIENT_DATA.update(tags);
    }
    
    public static void receiveRadioData(CompoundTag tags) {
        ItemStack stack = Minecraft.getInstance().player.getInventory().getArmor(2);
        if(stack.getItem() instanceof RadioItem radio) {
            radio.receiveData(stack, tags);
        }
    }
}
