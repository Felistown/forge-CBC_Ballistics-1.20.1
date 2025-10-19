package net.felis.cbc_ballistics.block.entity;

import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.OpenCoordinatorS2CPacket;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.SyncArtilleryNetS2CPacket;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.UpdateArtilleryNetDataS2CPacket;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.removeNetworkS2CPacket;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.util.ParticleHelper;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.felis.cbc_ballistics.util.artilleryNetwork.NetworkComponent;
import net.felis.cbc_ballistics.util.calculator.FiringSolutions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Set;

public class ArtilleryCoordinatorBlockEntity extends BlockEntity implements NetworkComponent {

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

    private int prevReady;

    double medianPres;
    double medianDisp;
    double medianTTT;

    private Mode mode;
    private int[] tPos;

    private ArtilleryCoordinatorBlockEntity superior;
    private final ArrayList<ArtilleryCoordinatorBlockEntity> suboridinates = new ArrayList<ArtilleryCoordinatorBlockEntity>();

    public ArtilleryCoordinatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ARTILLERY_COORDINATOR_BLOCK_ENTITY.get(), pPos, pBlockState);
        medianPres = 0.0;
        medianDisp = 0.0;
        medianTTT = 0.0;
        mode = Mode.INDIRECT;
    }


    public void removeSubordinate(ArtilleryCoordinatorBlockEntity block) {
        suboridinates.remove(block);
        setChanged();
    }

    public ArrayList<ArtilleryCoordinatorBlockEntity> getSuboridinates() {
        return suboridinates;
    }

    public void superiorOf(ArtilleryCoordinatorBlockEntity be) {
        be.setSuperior(this);
        if(!suboridinates.contains(be)) {
            suboridinates.add(be);
        }
        setChanged();
    }


    protected void setSuperior(ArtilleryCoordinatorBlockEntity block) {
        this.superior = block;
        setChanged();
    }

    public ArtilleryCoordinatorBlockEntity getSuperior() {
        return superior;
    }

    protected void removeSuborinate(ArtilleryCoordinatorBlockEntity block) {
        suboridinates.remove(block);
        setChanged();
    }

    public void removeSuperior() {
        superior.removeSuborinate(this);
        superior = null;
        setChanged();
    }

    @Override
    public BlockEntityType<ArtilleryCoordinatorBlockEntity> getType() {
        return ModBlockEntities.ARTILLERY_COORDINATOR_BLOCK_ENTITY.get();
    }

    public void onRemove() {
        for(Director d: directors) {
            d.setNetwork(null);
            removeNet(d.getBlockEntity().getBlockPos());
        }
        for(Layer c: cannons) {
            c.setNetwork(null);
            removeNet(c.getBlockEntity().getBlockPos());
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
        suboridinates.clear();
        superior = null;
        cannons.clear();
        directors.clear();
        safeSyncToClients();
    }

    public void setTarget(String targetPos) {
        setTargetPos(targetPos);
        if(tPos.length == 3) {
            for (Director d : directors) {
                d.setTarget(tPos);
            }
            for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
                be.setTarget(targetPos);
            }
        }
        ModMessages.sendToDimension(new UpdateArtilleryNetDataS2CPacket(new Artillery_CoordinatorInterface(this).getTags()), level);
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

    public void setMode(Mode mode) {
            CompoundTag tag = Utils.tagOf(this);
            tag.putByte("mode", mode.NUM);
            ModMessages.sendToDimension(new UpdateArtilleryNetDataS2CPacket(tag), level);
            this.mode = mode;
            for(Director d: directors) {
                d.mode(mode);
            }
            for(ArtilleryCoordinatorBlockEntity be: suboridinates) {
                be.setMode(mode);
            }

    }

    public void addCannon(Layer cannon, Director director) {
        if(!cannons.contains(cannon)) {
            cannons.add(cannon);
            cannon.setDirector(director);
            cannon.setNetwork(this);
            setChanged();
        }
    }

    public void addDirector(Director director) {
        if(!directors.contains(director)) {
            director.setId(uniqueDirectorId());
            director.setNetwork(this);
            directors.add(director);
            setChanged();
        }
    }

    public void addDirector(Director director, int id) {
        if(!directors.contains(director)) {
            director.setId(id);
            director.setNetwork(this);
            directors.add(director);
            setChanged();
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

    public void removeDirector(Director director) {
        for(Layer i: getLayers(director)) {
            i.setNetwork(null);
            cannons.remove(i);
            removeNet(i.getBlockEntity().getBlockPos());
        }
        director.setNetwork(null);
        directors.remove(director);
        safeSyncToClients();
        removeNet(director.getBlockEntity().getBlockPos());
        setChanged();
    }

    private void removeNet(BlockPos pos) {
        if(level != null && !level.isClientSide) {
            ModMessages.sendToPlayersRad(new removeNetworkS2CPacket(pos), Utils.targetPoint(pos, 200, level.dimension()));
            setChanged();
        }
    }

    public void removeCannon(Layer cannon) {
        cannon.setNetwork(null);
        cannons.remove(cannon);
        safeSyncToClients();
        removeNet(cannon.getBlockEntity().getBlockPos());
        setChanged();
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
                CompoundTag tag = Utils.tagOf(this);
                tag.putByte("ready", (byte)num);
                ModMessages.sendToDimension(new UpdateArtilleryNetDataS2CPacket(tag), level);
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

    public void appendForTooltip(Collection<Component> pTooltipComponents) {
        pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.shift"));
        String posS = " §e(" + Utils.blockPosToString(getBlockPos()) + ")§r";
        pTooltipComponents.add(Component.literal(NETWORK.getString() + network_id + posS));
        if(superior != null) {
            BlockPos sPos = superior.getBlockPos();
            String sPosS = " §e(" + Utils.blockPosToString(sPos) + ")§r";
            pTooltipComponents.add(Component.literal(SUPER.getString() + superior.network_id + sPosS));
        }
        if(!suboridinates.isEmpty()) {
            pTooltipComponents.add(SUB);
            for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
                BlockPos pos = be.getBlockPos();
                String bePosS = " §e(" + Utils.blockPosToString(pos) + ")§r";
                pTooltipComponents.add(Component.literal(ARROW.getString() + be.network_id + bePosS));
            }
        }
        if(!directors.isEmpty()) {
            pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.directors"));
            for (Director i : directors) {
                BlockPos pos1 = i.getBlockEntity().getBlockPos();
                String bePosS = " §e(" + Utils.blockPosToString(pos1) + ")§r";
                pTooltipComponents.add(Component.literal(DIRECTOR_ID.getString() + i.getId() + bePosS));
                for (Layer j : getLayers(i)) {
                    BlockPos pos2 = j.getBlockEntity().getBlockPos();
                    String bePosS2 = " §e(" + Utils.blockPosToString(pos2) + ")§r";
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
            tPos = array;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
            if (network_id != null) {
                pTag.putString("network_id", network_id);
            }
            int layers = 0;
            for (Director d : directors) {
                BlockPos dPos = d.getBlockEntity().getBlockPos();
                int[] dPosArray = {dPos.getX(), dPos.getY(), dPos.getZ(), d.getId()};
                pTag.putIntArray("d" + d.getId(), dPosArray);
                for (Layer l : getLayers(d)) {
                    BlockPos lPos = l.getBlockEntity().getBlockPos();
                    int[] lPosArray = {lPos.getX(), lPos.getY(), lPos.getZ(), d.getId()};
                    pTag.putIntArray("l" + layers, lPosArray);
                    layers++;
                }
            }
            if (superior != null) {
                pTag.putIntArray("u", Utils.blockPosToArray(superior.getBlockPos()));
            }
            for (int i = 0; i < suboridinates.size(); i++) {
                BlockPos pos = suboridinates.get(i).getBlockPos();
                pTag.putIntArray("s" + i, Utils.blockPosToArray(pos));
            }
        super.saveAdditional(pTag);
    }

    public CompoundTag getSaveData() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    public Mode getMode() {
        return mode;
    }

    public void reconnectNetwork(CompoundTag pTag) {
            removeAllData();
            network_id = pTag.getString("network_id");
            if (network_id.isEmpty()) {
                network_id = uniqueNetworkId();
            }
            if (level != null) {
                Set<String> keys = Set.copyOf(pTag.getAllKeys());
                for (String key : keys) {
                    if (key.charAt(0) == 'd') {
                        int[] data = pTag.getIntArray(key);
                        BlockPos pos = new BlockPos(data[0], data[1], data[2]);
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof Director) {
                            addDirector((Director) be, data[3]);
                        } else {
                        }
                    } else if (key.charAt(0) == 'u') {
                        int[] data = pTag.getIntArray(key);
                        BlockEntity be = level.getBlockEntity(Utils.arrayToBlockPos(data));
                        if (be instanceof ArtilleryCoordinatorBlockEntity) {
                            ((ArtilleryCoordinatorBlockEntity) be).superiorOf(this);
                        }
                    } else if (key.charAt(0) == 's') {
                        int[] data = pTag.getIntArray(key);
                        BlockEntity be = level.getBlockEntity(Utils.arrayToBlockPos(data));
                        if (be instanceof ArtilleryCoordinatorBlockEntity) {
                            superiorOf((ArtilleryCoordinatorBlockEntity) be);
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

    public void removeAllData() {
        if(superior != null) {
            removeSuperior();
        }
        for(int i = 0; i < suboridinates.size(); i ++) {
            removeSubordinate(suboridinates.get(i));
            i--;
        }
        for(int i = 0; i < cannons.size(); i ++) {
            removeCannon(cannons.get(i));
            i--;
        }
        for(int i = 0; i < directors.size(); i ++) {
            removeDirector(directors.get(i));
            i--;
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
    }

    @Override
    public void onLoad() {
        if (saveData != null) {
            reconnectNetwork(saveData);
        }
        if (network_id == null || network_id.isEmpty()) {
            network_id = uniqueNetworkId();
        }
        removeEmptyNetworks();
        networks.add(this);
        super.onLoad();
    }

    public void safeSyncToClient(Player player) {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            syncToClient(player);
        });
    }

    public void syncToClient(Player player) {
        ModMessages.sendToPlayer(new SyncArtilleryNetS2CPacket(getBlockPos(), getSaveData()), (ServerPlayer) player);
        for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
            be.syncToClient(player);
        }
    }

    public void safeSyncToClients() {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            syncToClients();
        });
    }

    public void syncToClients() {
        ModMessages.sendToPlayersRad(new SyncArtilleryNetS2CPacket(getBlockPos(), getSaveData()), Utils.targetPoint(getBlockPos(), 200, level.dimension()));
        for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
            be.syncToClients();
        }
    }

    public void openScreen(Player player) {
        ModMessages.sendToPlayer(new OpenCoordinatorS2CPacket(this), (ServerPlayer) player);
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
            FiringSolutions.Solution result = ((CalculatorBlockEntity)array.get(i)).getResult();
            if(result != null) {
                pres[i] = result.PRECISION;
                disp[i] = result.DISPERSION;
                tTT[i] = result.AIR_TIME;
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


    public static ArtilleryCoordinatorBlockEntity fromId(String id) {
        for(ArtilleryCoordinatorBlockEntity be: networks) {
            if(be.getNetwork_id().equals(id)) {
                return be;
            }
        }
        return null;
    }

    public void line(Level level, boolean isCentre) {
        try {
            if (level != null && level.isClientSide) {
                if (superior != null && isCentre) {
                    ParticleHelper.line(level, getBlockPos(), superior.getBlockPos(), ParticleHelper.Colour.YELLOW, 1);
                }
                for (ArtilleryCoordinatorBlockEntity be : suboridinates) {
                    ParticleHelper.line(level, getBlockPos(), be.getBlockPos(), ParticleHelper.Colour.WHITE, 1);
                    be.line(level, false);
                }
                for (Director d : directors) {
                    BlockPos pos = d.getBlockEntity().getBlockPos();
                    ParticleHelper.line(level, getBlockPos(), pos, ParticleHelper.Colour.BLUE, 1);
                    for (Layer l : getLayers(d)) {
                        ParticleHelper.line(level, pos, l.getBlockEntity().getBlockPos(), ParticleHelper.Colour.GREEN, 1);
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {}
    }

    @Override
    public ArtilleryCoordinatorBlockEntity getNetwork() {
        return this;
    }


    public void removeNetwork() {}

    public void removeNetwork(ArtilleryCoordinatorBlockEntity net) {
        if(superior != null && superior == net) {
            net.removeSubordinate(this);
            superior = null;
        } else if(suboridinates.contains(net)) {
            net.setSuperior(null);
            removeSubordinate(net);
        }
        setChanged();
    }

    public enum Mode {
        INDIRECT((byte)0),
        DIRECT((byte)1),
        PRECISE((byte)2),
        DISPERSION((byte)3);

        public final byte NUM;

        private Mode(byte num) {
            this.NUM = num;
        }

        public Mode next() {
            if(NUM + 1 <= 3) {
                return fromByte((byte)(NUM + 1));
            }
            return INDIRECT;
        }

        public static Mode fromByte(byte num) {
            for(Mode mode: Mode.class.getEnumConstants()) {
                if(mode.NUM == num) {
                    return mode;
                }
            }
            return null;
        }

        public Component getComponent() {
            return switch (NUM) {
                case 0 -> Component.translatable("block.cbc_ballistics.ballistic_calculator.indirect");
                case 1 -> Component.translatable("block.cbc_ballistics.ballistic_calculator.direct");
                case 2 -> Component.translatable("block.cbc_ballistics.artillery_network.pres");
                case 3 -> Component.translatable("block.cbc_ballistics.artillery_network.dis");
                default -> Component.literal("unknown material");
            };
        }
    }
}
