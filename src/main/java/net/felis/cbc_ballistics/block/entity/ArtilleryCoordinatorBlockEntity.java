package net.felis.cbc_ballistics.block.entity;

import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ArtilleryCoordinatorBlockEntity extends BlockEntity {

    private static final ArrayList<ArtilleryCoordinatorBlockEntity> networks = new ArrayList<ArtilleryCoordinatorBlockEntity>();
    private final ArrayList<Director> directors = new ArrayList<Director>();
    private final ArrayList<Layer> cannons = new ArrayList<Layer>();
    private final int NETWORK_ID;

    public ArtilleryCoordinatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ARTILLERY_COORDINATOR_BLOCK_ENTITY.get(), pPos, pBlockState);
        removeEmptyNetworks();
        NETWORK_ID = uniqueNetworkId();
        networks.add(this);
    }

    public void onRemove() {
        System.out.println("removed: " + NETWORK_ID);
        System.out.println("is client: " + level.isClientSide);
        for(Director d: directors) {
            d.setNetwork(null);
        }
        for(Layer c: cannons) {
            c.setNetwork(null);
        }
        //networks.remove(this);
        networks.remove(this);
        for(ArtilleryCoordinatorBlockEntity i: networks) {
            System.out.println(i.NETWORK_ID + "------------");
            for(Director d: i.getDirectors()) {
                System.out.println(d.getId() + "_+_+_+_+_+_");
                for(Layer c: i.getLayers(d)) {
                    System.out.println(((BlockEntity)c).getBlockPos().toString());
                }
            }
            System.out.println("------------");
        }
    }

    public boolean setTarget(int[] targetPos) {
        if(targetPos.length == 3) {
            for(Director i: directors) {
                i.setTarget(targetPos);
            }
            return true;
        } else {
            return false;
        }
    }

    public void addCannon(Layer cannon, Director director) {
        if(!cannons.contains(cannon)) {
            cannons.add(cannon);
            cannon.setDirector(director);
            cannon.setNetwork(this);
        }
    }

    public void addDirector(Director director) {
        if(!directors.contains(director)) {
            director.setId(uniqueDirectorId());
            director.setNetwork(this);
            directors.add(director);
        }
    }

    public ArrayList<Layer> getLayers(Director director) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        for(Layer i: cannons) {
            if(i.getDirector() == director) {
                layers.add(i);
            }
        }
        return layers;
    }

    public boolean removeDirector(Director director) {
        for(Layer i: getLayers(director)) {
            removeCannon(i);
        }
        director.setNetwork(null);
        return directors.remove(director);
    }

    public boolean removeCannon(Layer cannon) {
        cannon.setNetwork(null);
        return cannons.remove(cannon);
    }

    public int getNETWORK_ID() {
        return NETWORK_ID;
    }

    public ArrayList<Director> getDirectors() {
        return directors;
    }

    public ArrayList<Layer> getLayers() {
        return cannons;
    }

    private int uniqueDirectorId() {
        if(directors.isEmpty()) {
            return 0;
        }
        for(int i = 0; i < 1000; i ++) {
            boolean occupied = false;
            for(Director j: directors) {
                if(j.getId() == i) {
                    occupied = true;
                    break;
                }
            }
            if(!occupied) {
                return i;
            }
        }
        return -1;
    }

    public int numReadyLayers() {
        int num = 0;
        for(Layer i: cannons) {
            if(i.isSet()) {
                num++;
            }
        }
        return num;
    }

    public int numLayers() {
        return cannons.size();
    }

    private int uniqueNetworkId() {
        if(networks.isEmpty()) {
            return 0;
        }
        for(int i = 0; i < 1000; i ++) {
            boolean occupied = false;
            for(ArtilleryCoordinatorBlockEntity j: networks) {
                if (j.getNETWORK_ID() == i) {
                    occupied = true;
                    break;
                }
            }
            if(!occupied) {
                return i;
            }
        }
        return -1;
    }

    public void appendForTooltip(List<Component> pTooltipComponents) {
        pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.network").append("" + NETWORK_ID));
        pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.coordinators"));
        BlockPos pos = getBlockPos();
        pTooltipComponents.add(Component.literal("x = " + pos.getX() + ",y = " + pos.getY() + ",z = " + pos.getZ()));

        pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.directors"));
        for(Director i: directors) {
            BlockPos pos1 = i.getBlockEntity().getBlockPos();
            pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.directors.id").append("" + i.getId()).append(Component.translatable("unit.cbc_ballistics.artillery_network.at")).append( "x = " + pos1.getX() + ",y = " + pos1.getY() + ",z = " + pos1.getZ()));
            for (Layer j : getLayers(i)) {
                BlockPos pos2 = j.getBlockEntity().getBlockPos();
                pTooltipComponents.add(Component.literal("---x = " + pos2.getX() + ",y = " + pos2.getY() + ",z = " + pos2.getZ()));
            }
        }
    }

    private void removeEmptyNetworks() {
        for(int i = 0; i  < networks.size(); i ++) {
            ArtilleryCoordinatorBlockEntity net = networks.get(i);
            if(net.getDirectors().isEmpty() && net.getLayers().isEmpty()) {
                networks.remove(net);
                i --;
            }
        }
    }
}
