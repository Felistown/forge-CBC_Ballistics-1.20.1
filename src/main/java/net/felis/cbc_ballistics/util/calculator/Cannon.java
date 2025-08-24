package net.felis.cbc_ballistics.util.calculator;

import java.util.ArrayList;

public class Cannon {
    private final double[] coordinates = new double[3];
    private final int barrelLength;
    private final double maxPitch;
    private final double minPitch;
    private final String material;

    private Target target;

    private double gravity = 0.05;
    private double drag = 0.99;

    //Constructor methods
    public Cannon(double x, double y, double z, int barrelLength, double minPitch, double maxPitch, String material) {
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
        this.barrelLength = barrelLength;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.material = material;

        target = null;

        gravity = 0.05;
        drag = 0.99;
    }

    //mutator methods
    public void setTarget(Target target) {
        target.clear();
        this.target = target;
        target.setCannon(this);
    }
    public Projectile[] interpolateTarget(int charges) {
        Projectile[] solutions = calculate(charges);
        return new Projectile[]{calculate(solutions[0]), calculate(solutions[1])};
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setDrag(double drag) {
        this.drag = drag;
    }

    public Projectile[] interpolateTarget(int minCharge, int maxCharge) {
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
        return solutions;
    }

    //accessor methods
    public double getX() {
        return coordinates[0];
    }

    public double getY() {
        return coordinates[1];
    }

    public double getZ() {
        return coordinates[2];
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

    public String getMaterial() {
        return material;
    }

    public Target getTarget() {
        return target;
    }

    public double getGravity() {
        return gravity;
    }

    public double getDrag() {
        return drag;
    }

    public double getYaw() {
        return target.getYaw();
    }

    public double getDistance() {
        return target.getDistance();
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
}
