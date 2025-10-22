package net.felis.cbc_ballistics.util.calculator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class FiringSolutions {

    public final Solution INDIRECT;
    public final Solution DIRECT;
    public final float YAW;

    private FiringSolutions(float yaw) {
        YAW = yaw;
        DIRECT = new Solution();
        INDIRECT = new Solution();
    }

    public FiringSolutions() {
        this(0);
    }

    public FiringSolutions(Projectile[] projectiles, float yaw) {
        this.YAW = yaw;
        if(projectiles[0] != null) {
            DIRECT = new Solution(projectiles[0]);
        } else {
            DIRECT = new Solution();
        }
        if(projectiles[1] != null) {
            INDIRECT = new Solution(projectiles[1]);

        } else {
            INDIRECT = new Solution();
        }
    }

    public FiringSolutions(CompoundTag tag) {
        YAW = tag.getFloat("yaw");
        INDIRECT = new Solution(tag.getCompound("indirect"));
        DIRECT = new Solution(tag.getCompound("direct"));
    }

    public CompoundTag toTags() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("yaw", YAW);
        tag.put("direct", DIRECT.toTag());
        tag.put("indirect", INDIRECT.toTag());
        return tag;
    }

    public static FiringSolutions.Solution solution(CompoundTag tag) {
        return new FiringSolutions(tag.getFloat("yaw")).new Solution(tag);
    }

    public class Solution {

        public final float PITCH;
        public final Vec3 HIT_POS;
        public final float ANGLE_OF_ATTACK;
        public final float DISPERSION;
        public final float PRECISION;
        public final float AIR_TIME;
        public final byte CHARGES;
        public final float YAW;
        public final Type TYPE;

        public Solution() {
            PITCH = 0;
            DISPERSION = 0;
            PRECISION = 0;
            AIR_TIME = 0;
            CHARGES = 0;
            HIT_POS = new Vec3(0,0,0);
            ANGLE_OF_ATTACK = 90;
            TYPE = Type.OUT_OF_RANGE;
            this.YAW = FiringSolutions.this.YAW;
        }

        private Solution(Projectile projectile) {
            HIT_POS = projectile.simulate().getLocation();
            PITCH = (float) projectile.getPitch();
            DISPERSION = (float) projectile.getDispersion();
            PRECISION = (float) projectile.getPrecision();
            AIR_TIME = (float) projectile.getAirtime();
            CHARGES = (byte) projectile.getCharges();
            ANGLE_OF_ATTACK = projectile.getAngleOfAttack();
            TYPE = Type.NORMAl;
            this.YAW = FiringSolutions.this.YAW;
        }

        public Solution(CompoundTag tag) {
            PITCH = tag.getFloat("pitch");
            DISPERSION = tag.getFloat("disp");
            PRECISION = tag.getFloat("pres");
            AIR_TIME = tag.getFloat("air_time");
            CHARGES = tag.getByte("charges");
            ANGLE_OF_ATTACK = tag.getFloat("angle_of_attack");
            HIT_POS = new Vec3(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z"));
            TYPE = Type.of(tag.getBoolean("state"));
            this.YAW = FiringSolutions.this.YAW;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("pitch", PITCH);
            tag.putFloat("disp", DISPERSION);
            tag.putFloat("pres", PRECISION);
            tag.putFloat("air_time", AIR_TIME);
            tag.putByte("charges", CHARGES);
            tag.putBoolean("state", TYPE.BOOL);
            tag.putFloat("angle_of_attack", ANGLE_OF_ATTACK);
            tag.putFloat("x", (float)HIT_POS.x);
            tag.putFloat("y", (float)HIT_POS.y);
            tag.putFloat("z", (float)HIT_POS.z);
            return tag;
        }

        public CompoundTag toTagSingle() {
            CompoundTag tag = toTag();
            tag.putFloat("yaw", YAW);
            return tag;
        }
    }

    public enum Type {
        OUT_OF_RANGE(false),
        NORMAl(true);

        public final boolean BOOL;

        private Type(boolean bool) {
            BOOL = bool;
        }

        public static Type of(boolean bool) {
            if(bool) {
                return NORMAl;
            }
            return OUT_OF_RANGE;
        }
    }
}
