package net.felis.cbc_ballistics.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_CBC_BALLISTICS = "key.category.cbc_ballistics.cbc_ballistics";
    public static final String KEY_RANGEFIND =  "key.cbc_ballistics.rangefind";


    public static final KeyMapping RANGEFINDING_KEY = new KeyMapping(KEY_RANGEFIND, KeyConflictContext.IN_GAME, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, KEY_CATEGORY_CBC_BALLISTICS);


}
