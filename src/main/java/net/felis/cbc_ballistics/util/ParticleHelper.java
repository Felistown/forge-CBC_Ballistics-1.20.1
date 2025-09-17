package net.felis.cbc_ballistics.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;


public class ParticleHelper {

    private static final float FREQ = 0.25f;

    public static void line(Level level, BlockPos from, BlockPos to, Colour colour) {
        double[] f = Utils.vec3ToArray(from.getCenter());
        double[] t = Utils.vec3ToArray(to.getCenter());
        double dist = Utils.distFrom(from, to);
        double freak = dist / FREQ;
        float xChange = (float) ((t[0] - f[0]) / freak);
        float yChange = (float) ((t[1] - f[1]) / freak);
        float zChange = (float) ((t[2] - f[2]) / freak);
        DustParticleOptions dust = colour.toDust();
        for(float i = 0; i < dist; i += FREQ) {
            level.addParticle(dust, true, f[0], f[1], f[2], 0, 0,0);
            f[0] += xChange;
            f[1] += yChange;
            f[2] += zChange;
        }
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

        public DustParticleOptions toDust() {
            return new DustParticleOptions(new Vector3f(r, g, b), 1);
        }
    }

}
