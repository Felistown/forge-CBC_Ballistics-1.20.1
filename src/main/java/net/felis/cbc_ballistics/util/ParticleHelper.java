package net.felis.cbc_ballistics.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;


public class ParticleHelper {

    private static final float FREQ = 0.25f;

    public static void line(Level level, BlockPos from, BlockPos to, Colour colour, float size) {
        line(level, from.getCenter(), to.getCenter(), colour, size);
    }

    public static void line(Level level, Vec3 from, Vec3 to, Colour colour, float size) {
        double[] f = Utils.vec3ToArray(from);
        double[] t = Utils.vec3ToArray(to);
        double dist = Utils.distFrom(from, to);
        double freak = dist / FREQ;
        float xChange = (float) ((t[0] - f[0]) / freak);
        float yChange = (float) ((t[1] - f[1]) / freak);
        float zChange = (float) ((t[2] - f[2]) / freak);
        DustParticleOptions dust = colour.toDust(size);
        for(float i = 0; i < dist; i += FREQ) {
            level.addParticle(dust, true, f[0], f[1], f[2], 0, 0,0);
            f[0] += xChange;
            f[1] += yChange;
            f[2] += zChange;
        }
    }

    public static void dot(Level level, Vec3 pos, Colour colour, float size) {
        DustParticleOptions dust = colour.toDust(size);
        level.addParticle(dust, true, pos.x, pos.y, pos.z, 0, 0,0);
    }

    public static void Circle(Level level, Vec3 pos, Colour colour, float radius, float yaw, float pitch, float size) {
         dot(level, pos, colour, size);
         //TO DO



        //
    }

    public enum Colour {
        YELLOW(1, 1, 0.333333f),
        BLACK(0,0,0),
        WHITE(1,1,1),
        BLUE(0.333333f,0.333333f, 1),
        GREEN(0.333333f, 1, 0.333333f),
        RED(1, 0.333333f, 0.333333f);

        private final float r;
        private final float g;
        private final float b;

        private Colour(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public float[] toArray() {
            return new float[]{r, g, b};
        }

        public float r() {
            return r;
        }

        public float g() {
            return g;
        }

        public float b() {
            return b;
        }

        public DustParticleOptions toDust(float size) {
            return new DustParticleOptions(new Vector3f(r, g, b), size);
        }
    }

}
