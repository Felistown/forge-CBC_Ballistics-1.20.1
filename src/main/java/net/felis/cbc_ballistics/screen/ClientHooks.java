package net.felis.cbc_ballistics.screen;

import net.felis.cbc_ballistics.item.ModItems;
import net.felis.cbc_ballistics.screen.custom.Artillery_CoordinatorScreen;
import net.felis.cbc_ballistics.screen.custom.Ballistic_CalculatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ClientHooks {

    public static void openBallisticCalculatorScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new Ballistic_CalculatorScreen(pos));
    }

    public static void openArtilleryCoordinatorScreen(BlockPos pos, Artillery_CoordinatorInterface data) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                Player player = Minecraft.getInstance().player;
                Artillery_CoordinatorScreen screen = new Artillery_CoordinatorScreen(pos, data);
                ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                CompoundTag tag = item.getOrCreateTag();
                boolean send = false;
                if (item.getItem() == ModItems.RANGEFINDER.get() && tag.contains("results")) {
                    data.setTargetPos(item.getTag().getString("results"));
                    send = true;
                }
                Minecraft.getInstance().setScreen(screen);
                if(send) {
                    screen.send(null);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        });
    }
}
