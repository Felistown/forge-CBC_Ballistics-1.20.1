package net.felis.cbc_ballistics.screen;

import net.felis.cbc_ballistics.screen.custom.Ballistic_CalculatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientHooks {

    public static void openBallisticCalculatorScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new Ballistic_CalculatorScreen(pos));
    }
}
