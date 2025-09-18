package net.felis.cbc_ballistics.event;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.item.ModItems;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.RangefindC2SPacket;
import net.felis.cbc_ballistics.util.IHaveData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CBC_Ballistics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)

public class ModClientEvents {

    private boolean isScoped;
    private boolean normalise;
    private double sensitivity;

    public ModClientEvents() {
        isScoped = false;
        normalise = false;
        sensitivity = 100;
    }

    @SubscribeEvent
    public void onComputerFovModifierEvent(ComputeFovModifierEvent event) {
        if(!isScoped) {
            sensitivity = Minecraft.getInstance().options.sensitivity().get();
        }
        normalise = false;
        if(event.getPlayer().getUseItem().getItem() == ModItems.RANGEFINDER.get() && event.getPlayer().isUsingItem() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            isScoped = true;
            event.setNewFovModifier(0.1f);
            Minecraft.getInstance().options.sensitivity().set(sensitivity / 10f);
            normalise = true;
        } else {
            isScoped = false;
            if(!normalise) {
                Minecraft.getInstance().options.sensitivity().set(sensitivity);
            }
        }
    }

    @SubscribeEvent
    public void renderRangefinder(RenderGuiOverlayEvent event) {
        if(isScoped) {
            GuiGraphics graphics = event.getGuiGraphics();
            int screenWidth = graphics.guiWidth();
            int screenHeight = graphics.guiHeight();
            float f = (float)Math.min(screenWidth, screenHeight);
            int i = Mth.floor(f  * 1.125);
            int k = (screenWidth - i) / 2;
            int l = (screenHeight - i) / 2;
            int i1 = k + i;
            int j1 = l + i;
            graphics.blit(new ResourceLocation("cbc_ballistics:textures/misc/rangefinder.png"), k, l, -90, 0.0F, 0.0F, i, i, i, i);
            graphics.fill(RenderType.guiOverlay(), 0, j1, screenWidth, screenHeight, -90, -16777216);
            graphics.fill(RenderType.guiOverlay(), 0, 0, screenWidth, l, -90, -16777216);
            graphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
            graphics.fill(RenderType.guiOverlay(), i1, l, screenWidth, j1, -90, -16777216);
        }
        ItemStack item = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
        if(item.getItem() instanceof IHaveData) {
            GuiGraphics graphics = event.getGuiGraphics();
            int x = graphics.guiWidth() / 2;
            int y = (int)(graphics.guiHeight() * 0.75);
            graphics.drawCenteredString(Minecraft.getInstance().font, ((IHaveData)item.getItem()).getComponent(item), x, y, 16777215);
        }
    }

    @Mod.EventBusSubscriber(modid = CBC_Ballistics.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.MouseButton event) {
            Player player = Minecraft.getInstance().player;
            if(player != null && player.level().isClientSide) {
                ItemStack stack = player.getUseItem();
                int button = event.getButton();
                int action = event.getAction();
                if (button == 0 && action == 1 && player.isUsingItem() && stack.getItem() == ModItems.RANGEFINDER.get() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    ModMessages.sendToServer(new RangefindC2SPacket());
                }
            }
        }
    }

}
