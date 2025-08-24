package net.felis.cbc_ballistics.util.calculator;

public class Target {
    private double[] coordinates = new double[3];

    private double yaw = 0.0;

    private Cannon cannon = null;
    private double distance = 0.0;

    //Constructor methods
    public Target(double x, double y, double z, Cannon cannon) {
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
        this.cannon = cannon;
        double xDistance = cannon.getX() - coordinates[0];
        double zDistance = cannon.getZ() - coordinates[2];
        yaw = Math.atan(zDistance / xDistance) * (180 / Math.PI);
        double rawYaw = Math.atan(zDistance / (double)xDistance) * (180 / Math.PI);
        if(xDistance == 0) {
            rawYaw = 0;
        }
        if(xDistance >= 0) {
            rawYaw += 180;
        }
        yaw = (rawYaw + 270) % 360;
        distance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
    }

    //mutator methods

    public void setCannon(Cannon cannon) {
        this.cannon = cannon;
        double xDistance = cannon.getX() - coordinates[0];
        double zDistance = cannon.getZ() - coordinates[2];
        yaw = Math.atan(zDistance / xDistance) * (180 / Math.PI);
        double rawYaw = Math.atan(zDistance / (double)xDistance) * (180 / Math.PI);
        if(xDistance == 0) {
            rawYaw = 0;
        }
        if(xDistance >= 0) {
            rawYaw += 180;
        }
        yaw = (rawYaw + 270) % 360;
        distance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
    }

    public void clear() {
        cannon = null;
        yaw = 0.0;
        distance = 0.0;

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

    public double getYaw() {
        return yaw;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public double getDistance() {
        return distance;
    }

    //helper methods

}
