package net.felis.cbc_ballistics.util.calculator;

public class Projectile {
    private final Cannon cannon;
    private final double pitch;
    private final int power;
    private final double delta;
    private final double timeToTarget;

    private double[] coordinates = new double[2];
    private double[] velocity = new double[2];
    private double totalDistanceTravelled;

    private final Target target;
    private boolean simulated;
    private double precision;

    //Constructor methods
    public Projectile(Cannon cannon, double pitch, int charges) {
        this.cannon = cannon;
        this.pitch = pitch;
        power = charges * 2;
        simulated = false;

        coordinates[0] = cannon.getBarrelLength() * Math.cos(pitchRadians());
        coordinates[1] = cannon.getY() + Math.sin(pitchRadians()) * cannon.getBarrelLength();

        velocity[0] = Math.cos(pitchRadians()) * power;
        velocity[1] = Math.sin(pitchRadians()) * power;

        target = cannon.getTarget();

        timeToTarget = Math.abs(Math.log(1 - (target.getDistance() - coordinates[0]) / (100 * velocity[0])) / Math.log(0.99));
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

    public Target getTarget() {
        return target;
    }

    public double getPrecisionEstimate() {
        Double precisionEstimate = new Double(1 - delta / getAirtime());
        if(precisionEstimate.isNaN()) {
            return 0.0;
        } else {
            return precisionEstimate;
        }
    }

    public double getGravity() {
        return cannon.getGravity();
    }

    public double getDrag() {
        return cannon.getDrag();
    }

    public double getTotalDistanceTravelled() {
        return totalDistanceTravelled;
    }

    public double getDispersion() {
        if(!simulated) {
            precision = simulate();
            simulated = true;
        }
        double minDispersion;
        double spreadReductionPerBarrel;
        switch (cannon.getMaterial().toLowerCase()) {
            case "steel":
                minDispersion = 0.025;
                spreadReductionPerBarrel = 1.4;
                break;
            case "nethersteel":
                minDispersion = 0.02;
                spreadReductionPerBarrel = 1.15;
                break;
            case "castiron":
                minDispersion = 0.05;
                spreadReductionPerBarrel = 2;
                break;
            case "wroughtiron":
                minDispersion = 0.1;
                spreadReductionPerBarrel = 1;
                break;
            case "bronze":
                minDispersion = 0.03;
                spreadReductionPerBarrel = 1.4;
                break;
            default:
                throw new RuntimeException("invalid material");
        };
        double dispersion = Math.max(power * 2 - spreadReductionPerBarrel * cannon.getBarrelLength(), minDispersion);
        double angle = Math.atan((0.0172275 * dispersion) / power);
        return totalDistanceTravelled * Math.sin(angle);
    }

    public double getPrecision() {
        if (!simulated) {
            precision = simulate();
            simulated = true;
        }
        return precision;
    }

    private double simulate() {
        double xCoordinate = coordinates[0];
        double yCoordinate = coordinates[1];
        double xVelocity = velocity[0];
        double yVelocity = velocity[1];
        boolean isHitFloor = false;
        if(yCoordinate <= target.getY()) {
            while (yCoordinate + yVelocity <= target.getY()) {
                if(xCoordinate + xVelocity > target.getDistance()) {
                    break;
                }
                totalDistanceTravelled += Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
                xCoordinate += xVelocity;
                xVelocity *= cannon.getDrag();
                yCoordinate += yVelocity;
                yVelocity = cannon.getDrag() * yVelocity - cannon.getGravity();
            }
        }
        while (xCoordinate + xVelocity <= target.getDistance()) {
            if(yCoordinate + yVelocity <= target.getY()) {
                isHitFloor = true;
                break;
            }
            totalDistanceTravelled += Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
            xCoordinate += xVelocity;
            xVelocity *= cannon.getDrag();
            yCoordinate += yVelocity;
            yVelocity = cannon.getDrag() * yVelocity - cannon.getGravity();
        }
        if(isHitFloor) {
            double finalPitch = Math.atan(xVelocity / yVelocity);
            double yDisplacement = target.getY() - yCoordinate;
            double xDisplacement = Math.tan(finalPitch) * yDisplacement;
            xCoordinate += xDisplacement;
            totalDistanceTravelled += Math.sqrt(yDisplacement * yDisplacement+ xDisplacement * xDisplacement);
            return Math.abs(cannon.getDistance() - xCoordinate);
        } else {
            double finalPitch = Math.atan(yVelocity / xVelocity);
            double xDisplacement = target.getDistance() - xCoordinate;
            double yDisplacement = Math.tan(finalPitch) * xDisplacement;
            yCoordinate += yDisplacement;
            totalDistanceTravelled += Math.sqrt(yDisplacement * yDisplacement+ xDisplacement * xDisplacement);
            return Math.abs(target.getY() - yCoordinate);
        }
    }

    public String toString() {
        return "Pitch = " + getPitch() + ", precision = " + simulate() + ", Charges = " + getCharges() + ", traveltime = " + getAirtime() / 20 + ", dispersion = " + getDispersion();
    }

    //helper methods

    private double pitchRadians() {
        return Math.toRadians(pitch);
    }

    private double[] getAirTimes() {
        double yCoordinate = coordinates[1];
        double yVelocity = velocity[1];
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
