package net.felis.cbc_ballistics.util;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.util.calculator.Projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class Utils {

    public static boolean posFromString(String string, int[] array) {
            for(int i = 0; i <= 2; i ++) {
                boolean pass = false;
                boolean found = false;
                int index = 0;
                for (int j = 0; j < string.length(); j++) {
                    try {
                        Integer.valueOf(string.substring(j, j + 1));
                        if(!found) {
                            index = j;
                            found = true;
                        }
                    } catch (NumberFormatException e) {
                        if (found) {
                            try {
                                array[i] = Integer.parseInt(string.substring(index, j));
                            } catch (NumberFormatException e1) {
                                return false;
                            }
                            string = string.substring(j);
                            pass = true;
                            break;
                        } else {
                            if (string.charAt(j) == '-') {
                                index = j;
                                found = true;
                            }
                        }
                    }
                }
                if(!pass) {
                    if(found) {
                        try {
                            array[i] = Integer.parseInt(string.substring(index));
                            string = "";
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return string.isEmpty();
    }

    public static int stringToInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String blockPosToString(BlockPos pos) {
        return "X = " + pos.getX() + ", Y = " + pos.getY() + ", Z = " + pos.getZ();
    }

    public static int[] blockPosToArray(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    public static double[] vec3ToArray(Vec3 pos) {
        return new double[]{pos.x, pos.y, pos.z};
    }

    public static BlockPos arrayToBlockPos(int[] array) {
        return new BlockPos(array[0], array[1], array[2]);
    }

    public static double median(double[] array) {
        if(array.length > 0) {
            if(array.length > 1) {
                Arrays.sort(array.clone());
                return array[(int) Math.round(array.length / 2.0)];
            } else {
                return array[0];
            }
        }
        return 0.0;
    }

    public static float median(float[] array) {
        if(array.length > 0) {
            if(array.length > 1) {
                Arrays.sort(array.clone());
                return array[(int) Math.round(array.length / 2.0)];
            } else {
                return array[0];
            }
        }
        return 0.0f;
    }

    public static double[] toArray(ArrayList<Double> array) {
        double[] ret = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            ret[i] = array.get(i);
        }
        return ret;
    }

    public static String formatPos(int[] array) {
        return "X = " + array[0] + ", Y = " + array[1] + ", Z = " + array[2];
    }

    public static String formatPos(BlockPos pos) {
        return "X = " + pos.getX() + ", Y = " + pos.getY() + ", Z = " + pos.getZ();
    }

    public static String formatPos(Vec3 pos) {
        return "X = " + (int)Mth.floor(pos.x) + ", Y = " + (int)Mth.floor(pos.y) + ", Z = " + (int)Mth.floor(pos.z);
    }

    public static PacketDistributor.TargetPoint targetPoint(BlockPos pos, int radius, ResourceKey<Level> dimension) {
        return new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, dimension);
    }

    public static Vector3f vecToVel(float xRot, float yRot, float magnitude) {
        float xVel = -Mth.sin(yRot * 0.017453292F) * Mth.cos(xRot* 0.017453292F);
        float yVel = -Mth.sin(xRot * 0.017453292F);
        float zVel = Mth.cos(yRot * 0.017453292F) * Mth.cos(xRot * 0.017453292F);
        return new Vector3f(xVel * magnitude, yVel * magnitude, zVel * magnitude);
    }



    public static double distFrom(BlockPos pos0, BlockPos pos1) {
        int x = pos0.getX() -  pos1.getX();
        int y = pos0.getY() - pos1.getY();
        int z = pos0.getZ() - pos1.getZ();
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double distFrom(Vec3 pos0, Vec3 pos1) {
        double x = pos0.x -  pos1.x;
        double y = pos0.y - pos1.y;
        double z = pos0.z - pos1.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double distFrom(BlockEntity be0, BlockEntity be1) {
        return distFrom(be0.getBlockPos(), be1.getBlockPos());
    }

    public static boolean arrayEquals(int[] a0, int[] a1) {
        if(a0 == null || a1 == null) {
            return false;
        }
        for(int i = 0; i < Math.min(a0.length, a1.length); i ++) {
            if(a0[i] != a1[i]) {
                return false;
            }
        }
        return true;
    }

    public static CompoundTag tagOf(ArtilleryCoordinatorBlockEntity be) {
        CompoundTag tag = new CompoundTag();
        tag.putString("network_id", be.getNetwork_id());
        return tag;
    }

    public static int yawFromFacing(Direction d) {
        return switch (d) {
            case NORTH -> 0;
            case EAST -> 270;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        };
    }

    public static Vec3 orthog(Vec3 p) {
        if (p.x > p.z && p.x > p.y) {
            Vec3 vec = new Vec3(0, -p.z, p.y);
            return vec.scale(1 / vec.length());
        } else if(p.y > p.z) {
            Vec3 vec = new Vec3(-p.z, 0, p.x);;
            return vec.scale(1 / vec.length());
        } else {
            Vec3 vec = new Vec3(-p.y, p.x, 0);
            return vec.scale(1 / vec.length());
        }

    }
}
