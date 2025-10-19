package net.felis.cbc_ballistics.screen;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.felis.cbc_ballistics.util.ParticleHelper;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.calculator.FiringSolutions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Set;

public class Artillery_CoordinatorInterface {

    public static Artillery_CoordinatorInterface CLIENT_DATA;
    private CompoundTag tags;

    public Artillery_CoordinatorInterface(ArtilleryCoordinatorBlockEntity be) {
        tags = new CompoundTag();
        tags.putString("network_id", be.getNetwork_id());
        tags.putString("target", be.getPersistentData().getString("targetPos"));
        ArtilleryCoordinatorBlockEntity superior = be.getSuperior();
        if(superior != null) {
            tags.putString("superior", superior.getNetwork_id());
        }
        tags.putIntArray("pos", Utils.blockPosToArray(be.getBlockPos()));
        tags.putByte("mode", be.getMode().NUM);
        tags.putByte("guns", (byte) be.getAllCannons().size());
        tags.putByte("ready", (byte) be.numReadyLayers());
        tags.putByte("subnet", (byte) be.getSuboridinates().size());
        ArrayList<Director> directors = be.getAllDirectors();
        tags.putByte("dir", (byte) directors.size());
        for(int i = 0; i < directors.size(); i ++) {
            if(directors.get(i) instanceof CalculatorBlockEntity ce) {
                FiringSolutions.Solution results = ce.getResult();
                if(results != null) {
                    tags.put("solution" + i, results.toTagSingle());
                }
            }
        }
    }

    public Artillery_CoordinatorInterface(CompoundTag tags) {
        this.tags = tags;
    }

    public CompoundTag getTags() {
        return tags.copy();
    }

    public ArtilleryCoordinatorBlockEntity.Mode getMode() {
        return ArtilleryCoordinatorBlockEntity.Mode.fromByte(tags.getByte("mode"));
    }

    public void setMode(ArtilleryCoordinatorBlockEntity.Mode mode) {
        tags.putByte("mode", mode.NUM);
    }

    public String getNetworkId() {
        return tags.getString("network_id");
    }

    public String getSuperiorId() {
        return tags.getString("superior");
    }

    public byte getNumReadyCannons() {
        return tags.getByte("ready");
    }

    public String getTargetPos() {
        return tags.getString("target");
    }

    public void setTargetPos(String targetPos) {
        tags.putString("target", targetPos);
    }

    public boolean allSet() {
        return getNumCannons() == getNumReadyCannons();
    }

    public byte getNumSubnets() {
        return tags.getByte("subnet");
    }

    public byte getNumCannons() {
        return tags.getByte("guns");
    }

    public byte getNumDirector() {
        return tags.getByte("dir");
    }

    public void renderSolutions(Level level) {
        for(FiringSolutions.Solution solution: getSolutions()) {
            float impactYaw = (solution.YAW + 180) % 360;
            Vec3 thing = solution.HIT_POS.add(new Vec3(Utils.vecToVel(solution.ANGLE_OF_ATTACK, solution.YAW, -10)));
            ParticleHelper.line(level, solution.HIT_POS, thing, ParticleHelper.Colour.RED, 1);
            ParticleHelper.Circle(level, solution.HIT_POS, ParticleHelper.Colour.RED, solution.DISPERSION, impactYaw, solution.ANGLE_OF_ATTACK, 1);
        }
    }

    public float[] getMedianSet() {
        ArrayList<FiringSolutions.Solution> solutions = getSolutions();
        float[] pres = new float[solutions.size()];
        float[] disp = new float[solutions.size()];
        float[] tTT = new float[solutions.size()];
        for(int i = 0; i < solutions.size(); i ++) {
            FiringSolutions.Solution s = solutions.get(i);
                pres[i] = s.PRECISION;
                disp[i] = s.DISPERSION;
                tTT[i] = s.AIR_TIME;

        }
        /*  precision
            dispersion
            time to target  */
        return new float[]{Utils.median(pres), Utils.median(disp), Utils.median(tTT)};
    }

    public ArrayList<FiringSolutions.Solution> getSolutions() {
        ArrayList<FiringSolutions.Solution> solutions = new ArrayList<FiringSolutions.Solution>();
        Set<String> keys = tags.getAllKeys();
        for(String key: keys) {
            if(key.startsWith("solution")) {
                solutions.add(FiringSolutions.solution(tags.getCompound(key)));
            }
        }
        return solutions;
    }

    public boolean update(CompoundTag tags) {
        System.out.println("updating tags ------");
        if(tags.getString("network_id").equals(this.tags.getString("network_id"))) {
            System.out.println("successfuly updated tags ------");
            this.tags.merge(tags);
            return true;
        }
        return false;
    }


}
