package net.felis.cbc_ballistics.util.calculator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class Cannon {
    private BlockPos cannonPos;
    private Level level;
    private int barrelLength;
    private float maxPitch;
    private float minPitch;
    private Material material;
    private int maxCharge;
    private int minCharge;

    private float gravity;
    private float drag;

    private BlockPos target;

    //Constructor methods
    private Cannon(BlockPos cannonPos, int barrelLength, float minPitch, float maxPitch, Material material, Level level, int minCharge, int maxCharge, float gravity, float drag) {
        this.cannonPos = cannonPos;
        this.level = level;
        this.barrelLength = barrelLength;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.material = material;
        this.minCharge = minCharge;
        this.maxCharge = maxCharge;
        this.gravity = gravity;
        this.drag = drag;
    }

    //mutator methods
    public FiringSolutions interpolateTarget(BlockPos target) {
        this.target = target;
        Projectile[] solutions = new Projectile[2];
        ArrayList<Projectile> lowerSolutions = new ArrayList<Projectile>();
        ArrayList<Projectile> upperSolutions = new ArrayList<Projectile>();
        for(int i = minCharge; i <= maxCharge; i ++) {
            try {
                Projectile[] solutionI = calculate(i);
                lowerSolutions.add(calculate(solutionI[0]));
                upperSolutions.add(calculate(solutionI[1]));
            } catch (RuntimeException e) {
                continue;
            }
        }
        if(lowerSolutions.size() <= 0 || upperSolutions.size() <= 0) {
            throw new RuntimeException("Out of range");
        }
        solutions[0] = lowerSolutions.get(lowerSolutions.size() - 1);
        for(int i = lowerSolutions.size() - 1; i >= 0; i --) {
            if(lowerSolutions.get(i).getPrecisionEstimate() >= solutions[0].getPrecisionEstimate()) {
                solutions[0] = lowerSolutions.get(i);
            }
        }
        solutions[1] = upperSolutions.get(0);
        for(int i = 1; i < upperSolutions.size(); i ++) {
            if(upperSolutions.get(i).getPrecisionEstimate() >= solutions[1].getPrecisionEstimate()) {
                solutions[1] = upperSolutions.get(i);
            }
        }
        return new FiringSolutions(solutions, getYaw());
    }

    //accessor methods
    public BlockPos getCannonPos() {
        return cannonPos;
    }

    public Level getLevel() {
        return level;
    }

    public float getYaw() {
        double xDistance = cannonPos.getX() - target.getX();
        float rawYaw = (float) (Math.atan((cannonPos.getZ() - target.getZ()) / xDistance) * (180 / Math.PI));
        if(xDistance == 0) {
            rawYaw = 0;
        }
        if(xDistance >= 0) {
            rawYaw += 180;
        }
        return (rawYaw + 270) % 360;
    }

    public BlockPos getTarget() {
        return target;
    }

    public double distToTarget() {
        return Math.sqrt(Math.pow(cannonPos.getX() - target.getX(), 2) + Math.pow(cannonPos.getZ() - target.getZ(), 2));
    }

    public int getBarrelLength() {
        return barrelLength;
    }

    public double getMinPitch() {
        return minPitch;
    }

    public double getMaxPitch() {
        return maxPitch;
    }

    public Material getMaterial() {
        return material;
    }

    public float getGravity() {
        return gravity;
    }

    public float getDrag() {
        return drag;
    }

    //helper methods
    private Projectile[] calculate(int charges) {
        ArrayList<Projectile> angles = new ArrayList<Projectile>();
        for(int pitch = (int)Math.floor(minPitch); pitch <= (int)Math.ceil(maxPitch); pitch ++) {
            try {
                Projectile projectile = new Projectile(this, pitch, charges);
                angles.add(projectile);
            } catch (RuntimeException e) {
                continue;
            }
        }
        if(angles.size() <= 0) {
            throw new RuntimeException("Out of range");
        } else {
            return new Projectile[]{getUpperAngle(angles), getLowerAngle(angles)};
        }
    }

    private Projectile calculate(Projectile projectile) {
        for(int i = 0; i <= 5; i ++) {
            int charges = projectile.getCharges();
            double lowerPitch = projectile.getPitch() - Math.pow(10, -i);
            double upperPitch = projectile.getPitch() + Math.pow(10, -i);
            double iter = (upperPitch - lowerPitch) / 21;
            ArrayList<Projectile> angles = new ArrayList<Projectile>();
            for(double pitch = lowerPitch; pitch <= upperPitch; pitch += iter) {
                try {
                    Projectile newProjectile = new Projectile(this, pitch, charges);
                    angles.add(newProjectile);
                } catch (RuntimeException e) {
                    continue;
                }
            }
            projectile = angles.get(0);
            for(int j = 1; j < angles.size() ; j ++) {
                if(projectile.getDelta() > angles.get(j).getDelta()) {
                    projectile = angles.get(j);
                }
            }
        }
        return projectile;
    }

    private Projectile getUpperAngle(ArrayList<Projectile> array) {
        for(int i = 1; i < array.size() ; i ++) {
            if(array.get(i - 1).getDelta() < array.get(i).getDelta()) {
                return array.get(i - 1);
            }
        }
        return array.get(0);
    }

    private Projectile getLowerAngle(ArrayList<Projectile> array) {
        for(int i = array.size() - 2; i >= 0; i --) {
            if(array.get(i).getDelta() > array.get(i + 1).getDelta()) {
                return array.get(i + 1);
            }
        }
        return array.get(array.size() - 1);
    }

    public static class Builder {
        private BlockPos cannonPos;
        private Level level;
        private int barrelLength;
        private float maxPitch;
        private float minPitch;
        private Material material;
        private float gravity;
        private float drag;
        private int minCharge;
        private int maxCharge;

        public Builder(Level level) {
            cannonPos = new BlockPos(0,0,0);
            this.level = level;
            barrelLength = 1;
            material = Material.STEEL;
            maxPitch = 60;
            minPitch = -30;
            minCharge = 1;
            maxCharge = 1;
            gravity = 0.05f;
            drag = 0.99f;
        }

        public Builder at(BlockPos pos) {
            cannonPos = pos;
            return this;
        }

        public Builder length(int barrelLength) {
            this.barrelLength = barrelLength;
            return this;
        }

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder maxPitch(float maxPitch) {
            this.maxPitch = maxPitch;
            return this;
        }

        public Builder minPitch(float minPitch) {
            this.minPitch = minPitch;
            return this;
        }

        public Builder maxCharge(int maxCharge) {
            this.maxCharge = maxCharge;
            return this;
        }

        public Builder minCharge(int minCharge) {
            this.minCharge = minCharge;
            return this;
        }

        public Builder drag(float drag) {
            this.drag = drag;
            return this;
        }

        public Builder grav(float grav) {
            gravity = grav;
            return this;
        }

        public Cannon build() {
            return new Cannon(cannonPos, barrelLength, minPitch, maxPitch, material, level, minCharge, maxCharge, gravity, drag);
        }
    }
}
