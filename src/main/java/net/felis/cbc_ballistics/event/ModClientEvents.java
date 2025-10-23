package net.felis.cbc_ballistics.event;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.entity.model.RadioModel;
import net.felis.cbc_ballistics.item.ModItems;
import net.felis.cbc_ballistics.item.custom.RadioItem;
import net.felis.cbc_ballistics.item.custom.RangefinderItem;
import net.felis.cbc_ballistics.util.KeyBinding;
import net.felis.cbc_ballistics.util.ScreenDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CBC_Ballistics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    public static final ScreenDisplay SCREEN = new ScreenDisplay();
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
        GuiGraphics graphics = event.getGuiGraphics();
        if(isScoped) {
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
        SCREEN.display(graphics);
    }

    @Mod.EventBusSubscriber(modid = CBC_Ballistics.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onKeyInputMouse(InputEvent.MouseButton event) {
            Player player = Minecraft.getInstance().player;
            if(player != null && player.level().isClientSide) {
                ItemStack stack = player.getUseItem();
                int button = event.getButton();
                int action = event.getAction();
                if (button == 0 && action == 1 && player.isUsingItem() && stack.getItem() == ModItems.RANGEFINDER.get() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    ItemStack thing = player.getUseItem();
                    Item item = thing.getItem();
                    if (item instanceof RangefinderItem rangefinder) {
                        rangefinder.rangeFind(thing);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInputKey(InputEvent.Key event) {
            Player player = Minecraft.getInstance().player;
            if(KeyBinding.OPEN_RADIO_KEY.consumeClick()) {
                System.out.println("key output");
                ItemStack chestplate = player.getInventory().armor.get(2);
                if(chestplate.getItem() instanceof RadioItem radio) {
                    if(chestplate.getOrCreateTag().contains("network")) {
                        radio.openRadio(chestplate);
                    } else {
                        ModClientEvents.SCREEN.add(Component.translatable("item.cbc_ballistics.radio_item.empty"), 6000);
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = CBC_Ballistics.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.OPEN_RADIO_KEY);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(RadioModel.LAYER_LOCATION, RadioModel::createBodyLayer);
        }
    }
}
