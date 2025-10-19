package net.felis.cbc_ballistics.util.calculator;

import net.minecraft.network.chat.Component;

public enum Material {
    STEEL(1.4f, 0.025f, (byte) 3),
    NETHER_STEEL(1.15f, 0.02f, (byte) 4),
    CAST_IRON(2f, 0.05f, (byte) 0),
    WROUGHT_IRON(1f, 0.1f, (byte) 1),
    BRONZE(1.4f, 0.03f, (byte) 2);

    public final float reduction;
    public final float minDisp;
    public final byte no;

    private Material(float reduction, float minDisp, byte no) {
        this.reduction = reduction;
        this.minDisp = minDisp;
        this.no = no;
    }

    public static Material fromInt(int num) {
        for(Material m: Material.class.getEnumConstants()) {
            if(m.no == num) {
                return m;
            }
        }
        return null;
    }

    public static Material fromString(String str) {
        switch (str.toLowerCase()) {
            case "cast_iron":
                return CAST_IRON;
            case "wrought_iron":
                return WROUGHT_IRON;
            case "bronze":
                return BRONZE;
            case "steel":
                return STEEL;
            case "nether_steel":
                return NETHER_STEEL;
            default:
                return null;
        }
    }

    public Material next() {
        if(no + 1 > 4) {
            return CAST_IRON;
        } else {
            return fromInt(no + 1);
        }
    }

    public Component getComponent() {
        switch (no) {
            case 0:
                return Component.translatable("block.cbc_ballistics.ballistic_calculator.castIron");
            case 1:
                return Component.translatable("block.cbc_ballistics.ballistic_calculator.wroughtIron");
            case 2:
                return  Component.translatable("block.cbc_ballistics.ballistic_calculator.bronze");
            case 3:
                return Component.translatable("block.cbc_ballistics.ballistic_calculator.steel");
            case 4:
                return Component.translatable("block.cbc_ballistics.ballistic_calculator.netherSteel");
            default:
                return Component.literal("unknown material");
        }
    }
}
