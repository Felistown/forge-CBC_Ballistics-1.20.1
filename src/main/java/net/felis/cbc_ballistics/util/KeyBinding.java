package net.felis.cbc_ballistics.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_CBC_BALLISTICS = "key.category.cbc_ballistics.cbc_ballistics";
    public static final String KEY_OPEN_RADIO = "key.cbc_ballistics.radio";
    
    public static final KeyMapping OPEN_RADIO_KEY = new KeyMapping(
            KEY_OPEN_RADIO,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            KEY_CATEGORY_CBC_BALLISTICS
    );
}
