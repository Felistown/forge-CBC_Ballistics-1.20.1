package net.felis.cbc_ballistics.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CBC_BallisticsCommonConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> RANGEFINDER_MAX_RANGE;

    static {
        BUILDER.push("Common configs for CBC_Ballistics");

        RANGEFINDER_MAX_RANGE = BUILDER.comment("Maximum range for rangefinder (Increasing this value will lag servers)").define("Maximum rangefinder range in blocks", 640);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
