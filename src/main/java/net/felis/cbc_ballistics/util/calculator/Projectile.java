package net.felis.cbc_ballistics.util.calculator;

import net.felis.cbc_ballistics.entity.custom.DetectingProjectile;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Projectile {
    private final Cannon cannon;
    private final double pitch;
    private final int power;
    private final double delta;
    private final double timeToTarget;
    private float angle_of_attack;

    private double[] pos = new double[2];
    private double[] vel = new double[2];

    private HitResult result;
    private double distanceTravelled;

    //Constructor methods
    public Projectile(Cannon cannon, double pitch, int charges) {
        this.cannon = cannon;
        this.pitch = pitch;
        power = charges * 2;
        double pitchRadians = Math.toRadians(pitch);

        BlockPos cannonPos = cannon.getCannonPos();
        pos[0] = cannon.getBarrelLength() * Math.cos(pitchRadians);
        pos[1] = cannonPos.getY() + Math.sin(pitchRadians) * cannon.getBarrelLength();

        vel[0] = Math.cos(pitchRadians) * power;
        vel[1] = Math.sin(pitchRadians) * power;

        timeToTarget = Math.abs(Math.log(1 - (cannon.distToTarget() - pos[0]) / (100 * vel[0])) / Math.log(0.99));
        double[] airTimes = getAirTimes();
        double deltaA = Math.abs(timeToTarget - airTimes[0]);
        double deltaB = Math.abs(timeToTarget - airTimes[1]);
        delta = Math.min(deltaA, deltaB);
    }



    //mutator methods

    //accessor methods
    public Cannon getCannon() {
        return cannon;
    }

    public float getYaw() {
        return cannon.getYaw();
    }

    public double getPitch() {
        return pitch;
    }

    public int getCharges() {
        return power / 2;
    }

    public double getDelta() {
        return  delta;
    }

    public double getTimeToTarget() {
        return timeToTarget;
    }

    public double getAirtime() {
        return timeToTarget + delta;
    }

    public double getPrecisionEstimate() {
        Double precisionEstimate = new Double(1 - delta / getAirtime());
        if(precisionEstimate.isNaN()) {
            return 0.0;
        } else {
            return precisionEstimate;
        }
    }

    public double getDispersion() {
        Material material = cannon.getMaterial();
        double inaccuracy = Math.max(power - material.reduction * cannon.getBarrelLength(), material.minDisp);
        return 0.0172275 * inaccuracy * distanceTravelled;
    }

    public HitResult simulate() {
        Vec3 cPos  = cannon.getCannonPos().getCenter();
        float yaw = cannon.getYaw();
        Vector3f tipPos = Utils.vecToVel(-(float)pitch , yaw, cannon.getBarrelLength() + 0.5f);
        tipPos.add((float)cPos.x, (float)cPos.y, (float)cPos.z);
        DetectingProjectile d = new DetectingProjectile.Detect(cannon.getLevel(), new Vec3(tipPos))
                .grav(cannon.getGravity())
                .drag(cannon.getDrag())
                .allowHitEntity(false)
                .range(1000000)
                .simulate(-(float)pitch , yaw, power);
        result = d.getResults();
        angle_of_attack = d.getAngle_of_attack();
        distanceTravelled = d.getDistTravelled();
        return result;
    }

    public float getAngleOfAttack() {
        return angle_of_attack;
    }

    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public double getPrecision() {
        if(result != null) {
            BlockPos cPos = cannon.getTarget();
            return Utils.distFrom(result.getLocation(), new Vec3(cPos.getX(), cPos.getY(), cPos.getZ()));
        } else {
            return Double.MAX_VALUE;
        }
    }

    public String toString() {
        return "Pitch = " + getPitch() + ", precision = " + getPrecision() + ", Charges = " + getCharges() + ", traveltime = " + getAirtime() / 20 + ", dispersion = " + getDispersion();
    }

    //helper methods

    private double[] getAirTimes() {
        BlockPos target = cannon.getTarget();
        double yCoordinate = pos[1];
        double yVelocity = vel[1];
        double airTime0 = 0;
        double airTime1 = 999999999;
        if(yCoordinate <= target.getY()) {
            while(airTime0 < 100000) {
                yCoordinate += yVelocity;
                yVelocity = cannon.getDrag() * yVelocity - cannon.getGravity();
                airTime0 ++;
                if(yCoordinate > target.getY()) {
                    airTime1 = airTime0 - 1;
                    break;
                }
                if(yVelocity <= 0) {
                    throw new RuntimeException("Out of range");
                }
            }
        }
        while(airTime0 < 100000) {
            yCoordinate += yVelocity;
            yVelocity = cannon.getDrag() * yVelocity - cannon.getGravity();
            airTime0 ++;
            if(yCoordinate <= target.getY()) {
                return new double[]{airTime0, airTime1};
            }
        }
        throw new RuntimeException("Out of range");
    }
}
