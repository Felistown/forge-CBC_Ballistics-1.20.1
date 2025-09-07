package net.felis.cbc_ballistics.block.entity;

import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SendReadyCannonsS2CPacket;
import net.felis.cbc_ballistics.networking.packet.SyncArtilleryNetS2CPacket;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.felis.cbc_ballistics.util.calculator.Projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArtilleryCoordinatorBlockEntity extends BlockEntity {

    private static final Component NETWORK = Component.translatable("unit.cbc_ballistics.artillery_network.network");
    private static final Component AT = Component.translatable("unit.cbc_ballistics.artillery_network.at");
    private static final Component SUB = Component.translatable("unit.cbc_ballistics.artillery_network.sub");
    private static final Component SUPER = Component.translatable("unit.cbc_ballistics.artillery_network.super");
    private static final Component DIRECTOR_ID = Component.translatable("unit.cbc_ballistics.artillery_network.directors.id");
    private static final Component CANNON = Component.translatable("block.cbc_ballistics.artillery_coordinator.cannon");
    private static final Component ARROW = Component.translatable("unit.cbc_ballistics.artillery_network.arrow");

    private CompoundTag saveData;

    private static final ArrayList<ArtilleryCoordinatorBlockEntity> networks = new ArrayList<ArtilleryCoordinatorBlockEntity>();
    private final ArrayList<Director> directors = new ArrayList<Director>();
    private final ArrayList<Layer> cannons = new ArrayList<Layer>();
    private String network_id;

    private boolean ticked;

    private int prevReady;

    double medianPres;
    double medianDisp;
    double medianTTT;

    private int mode;
    private int[] tPos;
    private boolean changedTarget;
    /*
     0 = indirect
     1 = direct
     2 = best precision
     3 = best dispersion
     */

    private ArtilleryCoordinatorBlockEntity superior;
    private final ArrayList<ArtilleryCoordinatorBlockEntity> suboridinates = new ArrayList<ArtilleryCoordinatorBlockEntity>();

    public ArtilleryCoordinatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ARTILLERY_COORDINATOR_BLOCK_ENTITY.get(), pPos, pBlockState);
        ticked = false;
        medianPres = 0.0;
        medianDisp = 0.0;
        medianTTT = 0.0;
    }


    public void removeSubordinate(ArtilleryCoordinatorBlockEntity block) {
        suboridinates.remove(block);
    }

    public ArrayList<ArtilleryCoordinatorBlockEntity> getSuboridinates() {
        return suboridinates;
    }

    public void superiorOf(ArtilleryCoordinatorBlockEntity be) {
        if(!suboridinates.contains(be) && superior != be) {
            suboridinates.add(be);
            be.setSuperior(this);
        }
    }


    protected void setSuperior(ArtilleryCoordinatorBlockEntity block) {
        this.superior = block;

    }

    public ArtilleryCoordinatorBlockEntity getSuperior() {
        return superior;
    }

    public void removeSuperior() {
        superior = null;
    }

    @Override
    public BlockEntityType<ArtilleryCoordinatorBlockEntity> getType() {
        return ModBlockEntities.ARTILLERY_COORDINATOR_BLOCK_ENTITY.get();
    }

    public void onRemove() {
        for(Director d: directors) {
            d.setNetwork(null);
        }
        for(Layer c: cannons) {
            c.setNetwork(null);
        }
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            be.setSuperior(null);
            be.setChanged();
        }
        if(superior != null) {
            superior.removeSubordinate(this);
            superior.setChanged();
        }
        networks.remove(this);
    }

    public void setTarget(String targetPos) {
        setTargetPos(targetPos);
        changedTarget = false;
        if(tPos.length == 3) {
            for (Director d : directors) {
                d.setTarget(tPos);
            }
            for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
                be.setTarget(targetPos);
            }
        }
        double[] array = getMedianSet();
        medianDisp = array[1];
        medianPres = array[0];
        medianTTT = array[2];
    }

    public void fire() {
        for (Layer c : cannons) {
            c.fire();
        }
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            be.fire();
        }
    }

    public int[] getTargetPos() {
        return tPos;
    }

    public void target() {
        for (Director d : directors) {
            d.target();
        }
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            be.target();
        }
    }

    public void changeMode() {
        mode ++;
        if(mode > 3) {
            mode = 0;
        }
        for(Director d: directors) {
            d.mode(mode);
        }
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            be.setMode(mode);
        }
    }

    public void setMode(int mode) {
        if(mode <= 2 && mode >= 0)  {
            this.mode = mode;
            for(Director d: directors) {
                d.mode(mode);
            }
            for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
                be.setMode(mode);
            }
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

    public void addDirector(Director director, int id) {
        if(!directors.contains(director)) {
            director.setId(id);
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

    public String getNetwork_id() {
        return network_id;
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
        for(Layer i: getAllCannons()) {
            if(i.isSet()) {
                num++;
            }
        }
        if(num != prevReady) {
            if(level != null) {
                int num0 = num;
                //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> ()->
                        ModMessages.sendToPlayersRad(new SendReadyCannonsS2CPacket(getBlockPos(), num0), Utils.targetPoint(getBlockPos(), 160, level.dimension()));
                //);
            }
        }
        prevReady = num;
        return num;
    }

    public boolean allSet() {
        for(Director i: getAllDirectors()) {
            if(!((CalculatorBlockEntity)i).isSet()) {
                return false;
            }
        }
        return true;
    }

    private String uniqueNetworkId() {
        boolean found = false;
        while(!found) {
            String id = "";
            for(int i = 0; i < 5; i++) {
                id += (char)((Math.random() * 26) + 97);
            }

            found = true;
            for(ArtilleryCoordinatorBlockEntity be: networks) {
                if(be.getNetwork_id() != null && be.getNetwork_id().equals(id)) {
                    found = false;
                }
            }
            if(found) {
                return id;
            }
        }
        return null;
    }

    public void appendForTooltip(List<Component> pTooltipComponents, boolean shiftDown) {
        pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.shift"));
        String posS = "";
        if(shiftDown) {
            posS = " §e(" + Utils.blockPosToString(getBlockPos()) + ")§r";
        }
        pTooltipComponents.add(Component.literal(NETWORK.getString() + network_id + posS));
        if(superior != null) {
            BlockPos sPos = superior.getBlockPos();
            String sPosS = "";
            if(shiftDown) {
                sPosS = " §e(" + Utils.blockPosToString(sPos) + ")§r";
            }
            pTooltipComponents.add(Component.literal(SUPER.getString() + superior.network_id + sPosS));
        }
        if(!suboridinates.isEmpty()) {
            pTooltipComponents.add(SUB);
            for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
                BlockPos pos = be.getBlockPos();
                String bePosS = "";
                if(shiftDown) {
                    bePosS = " §e(" + Utils.blockPosToString(pos) + ")§r";
                }
                pTooltipComponents.add(Component.literal(ARROW.getString() + be.network_id + bePosS));
            }
        }
        if(!directors.isEmpty()) {
            pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.directors"));
            for (Director i : directors) {
                BlockPos pos1 = i.getBlockEntity().getBlockPos();
                String bePosS = "";
                if (shiftDown) {
                    bePosS = " §e(" + Utils.blockPosToString(pos1) + ")§r";
                }
                pTooltipComponents.add(Component.literal(DIRECTOR_ID.getString() + i.getId() + bePosS));
                for (Layer j : getLayers(i)) {
                    BlockPos pos2 = j.getBlockEntity().getBlockPos();
                    String bePosS2 = "";
                    if (shiftDown) {
                        bePosS2 = " §e(" + Utils.blockPosToString(pos2) + ")§r";
                    }
                    pTooltipComponents.add(Component.literal(CANNON.getString() + bePosS2));
                }
            }
        }
    }

    private void removeEmptyNetworks() {
        for(int i = 0; i  < networks.size(); i ++) {
            ArtilleryCoordinatorBlockEntity net = networks.get(i);
            if(net.getDirectors().isEmpty() && net.getLayers().isEmpty() && net.getSuperior() == null && net.getSuboridinates().isEmpty()) {
                networks.remove(net);
                i --;
            }
        }
    }

    public Director getDirector(int id) {
        for(Director d: directors) {
            if(d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    public boolean setTargetPos(String targetPos) {
        getPersistentData().putString("targetPos", targetPos);
        setChanged();
        int[] array = new int[3];
        boolean result = Utils.posFromString(targetPos, array);
        if(result) {
            if(!Utils.arrayEquals(tPos, array)) {
                changedTarget = true;
            }
            tPos = array;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if(network_id != null) {
            pTag.putString("network_id", network_id);
        }
        int layers = 0;
        for(Director d: directors) {
            BlockPos dPos = d.getBlockEntity().getBlockPos();
            int[] dPosArray = {dPos.getX(), dPos.getY(), dPos.getZ(), d.getId()};
            pTag.putIntArray("d" + d.getId(),dPosArray);
            ArrayList<Layer> cannons = getLayers(d);
            for(Layer l: getLayers(d)) {
                BlockPos lPos = l.getBlockEntity().getBlockPos();
                int[] lPosArray = {lPos.getX(), lPos.getY(), lPos.getZ(), d.getId()};
                pTag.putIntArray("l" + layers, lPosArray);
                layers ++;
            }
        }
        if(superior != null) {
            pTag.putIntArray("u", Utils.blockPosToArray(superior.getBlockPos()));
        }
        for(int i = 0; i < suboridinates.size(); i ++) {
            BlockPos pos = suboridinates.get(i).getBlockPos();
            pTag.putIntArray("s" + i, Utils.blockPosToArray(pos));
        }
        super.saveAdditional(pTag);
    }

    public int getMode() {
        return mode;
    }

    public void reconnectNetwork(CompoundTag pTag) {
        superior = null;
        suboridinates.clear();
        cannons.clear();
        directors.clear();
        network_id = pTag.getString("network_id");
        if(network_id.isEmpty()) {
            network_id = uniqueNetworkId();
        }
        if(level != null) {
            Set<String> keys = pTag.getAllKeys();
            for (String key : keys) {
                if (key.charAt(0) == 'd') {
                    int[] data = pTag.getIntArray(key);
                    BlockPos pos = new BlockPos(data[0], data[1], data[2]);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Director) {
                        addDirector((Director) be, data[3]);
                    }
                } else if(key.charAt(0) == 'u') {
                    int[] data = pTag.getIntArray(key);
                    BlockEntity be = level.getBlockEntity(Utils.arrayToBlockPos(data));
                    if (be instanceof ArtilleryCoordinatorBlockEntity) {
                        ((ArtilleryCoordinatorBlockEntity) be).superiorOf(this);
                    }
                } else if(key.charAt(0) == 's') {
                    int[] data = pTag.getIntArray(key);
                    BlockEntity be = level.getBlockEntity(Utils.arrayToBlockPos(data));
                    if (be instanceof ArtilleryCoordinatorBlockEntity) {
                        superiorOf((ArtilleryCoordinatorBlockEntity)be);
                    }
                }
            }
            for (String key : keys) {
                if (key.charAt(0) == 'l') {
                    int[] data = pTag.getIntArray(key);
                    BlockPos pos = new BlockPos(data[0], data[1], data[2]);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Layer) {
                        addCannon((Layer) be, getDirector(data[3]));
                    }
                }
            }
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        saveData = pTag;
        pTag.merge(getPersistentData());
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    public void tick() {
        numReadyLayers();
        if(!ticked) {
            ticked = true;
                if (saveData != null) {
                    reconnectNetwork(saveData);
                    setChanged();
                }
                if (network_id == null) {
                    network_id = uniqueNetworkId();
                }
                removeEmptyNetworks();
                networks.add(this);
        }
    }

    public void syncToClient(Player player) {
        //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> ()->
                ModMessages.sendToPlayer(new SyncArtilleryNetS2CPacket(getBlockPos(), getUpdateTag()), (ServerPlayer)player );
        //);
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            be.syncToClient(player);
        }
    }

    public ArtilleryCoordinatorBlockEntity topHierarchy() {
        if(superior != null) {
            return superior.topHierarchy();
        } else {
            return this;
        }
    }

    public ArrayList<Director> getAllDirectors() {
        ArrayList<Director> array = (ArrayList<Director>) directors.clone();
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            array.addAll(be.getAllDirectors());
        }
        return array;
    }

    public ArrayList<Layer> getAllCannons() {
        ArrayList<Layer> array = (ArrayList<Layer>) cannons.clone();
        for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
            array.addAll(be.getAllCannons());
        }
        return array;
    }

    public double[] getMedianSet() {
        //double[]{precision, dispersion, time to target}
        ArrayList<Director> array = getAllDirectors();
        double[] pres = new double[array.size()];
        double[] disp = new double[array.size()];
        double[] tTT = new double[array.size()];
        for(int i = 0; i < array.size(); i ++) {
            Projectile result = ((CalculatorBlockEntity)array.get(i)).getResult();
            if(result != null) {
                pres[i] = result.getPrecision();
                disp[i] = result.getDispersion();
                tTT[i] = result.getAirtime();
            }
        }
        return new double[]{Utils.median(pres), Utils.median(disp), Utils.median(tTT)};
    }

    public double getMedianDisp() {
        return medianDisp;
    }

    public double getMedianPres() {
        return medianPres;
    }

    public double getMedianTTT() {
        return medianTTT;
    }

    public boolean changedTarget() {
        return changedTarget;
    }

    public ArrayList<ArtilleryCoordinatorBlockEntity> allInSystem() {
        ArrayList<ArtilleryCoordinatorBlockEntity> ret = new ArrayList<ArtilleryCoordinatorBlockEntity>();
        topHierarchy().allInSystem(ret);
        return ret;
    }

    protected ArrayList<ArtilleryCoordinatorBlockEntity> allInSystem(ArrayList<ArtilleryCoordinatorBlockEntity> array) {
        if(superior != null) {
            array.add(superior);
        }
        if(suboridinates.isEmpty()) {
            array.add(this);
        } else {
            for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
                be.allInSystem(array);
            }
        }
        return array;
    }

    public boolean partOfNetwork(ArtilleryCoordinatorBlockEntity be) {
        return allInSystem().contains(be);
    }
}
