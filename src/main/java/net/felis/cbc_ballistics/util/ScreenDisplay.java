package net.felis.cbc_ballistics.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ScreenDisplay {

    private static final Font FONT = Minecraft.getInstance().font;
    private Component component;
    private int lifeTime;

    public ScreenDisplay() {
        component = Component.empty();
    }

    public void display(GuiGraphics graphics) {
        int x = graphics.guiWidth() / 2;
        int y = (int)(graphics.guiHeight() * 0.75);
        if(lifeTime > 0) {
            lifeTime --;
            graphics.drawCenteredString(Minecraft.getInstance().font, component, x, y, 16777215);
            if(lifeTime == 0) {
                component = Component.empty();
            }
        } else {
            ItemStack stack = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() instanceof IHaveData item) {
                graphics.drawCenteredString(Minecraft.getInstance().font, item.getComponent(stack), x, y, 16777215);
            }
        }
    }

    public void add(Component component, int lifeTime) {
        this.component = component;
        this.lifeTime = lifeTime;
    }

    public void clear() {
        component = Component.empty();
        lifeTime = 0;
    }
}
