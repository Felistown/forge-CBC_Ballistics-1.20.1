package net.felis.cbc_ballistics.screen.custom;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.SendArtilleryNetworkInstructionC2SPacket;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class Artillery_CoordinatorScreen extends Screen {

    //private static final Component = Component.translatable();
    private static final ResourceLocation BACKGROUND = new ResourceLocation(CBC_Ballistics.MODID, "textures/gui/artillery_network/artillery_network_screen.png");
    private static final Font FONT = Minecraft.getInstance().font;
    private static final Component ID = Component.translatable("block.cbc_ballistics.artillery_network.id");
    private static final Component NUM_DIR = Component.translatable("block.cbc_ballistics.artillery_coordinator.num_dir");
    private static final Component NUM_CAN = Component.translatable("block.cbc_ballistics.artillery_coordinator.num_can");
    private static final Component READY_CAN = Component.translatable("block.cbc_ballistics.artillery_network.ready_can");
    private static final Component SEND = Component.translatable("block.cbc_ballistics.artillery_network.send");
    private static final Component SET = Component.translatable("block.cbc_ballistics.artillery_network.set");
    private static final Component FIRE = Component.translatable("block.cbc_ballistics.artillery_network.fire");
    private static final Component TARGET_POS = Component.translatable("block.cbc_ballistics.ballistic_calculator.targetPos");
    private static final Component MEDIAN = Component.translatable("block.cbc_ballistics.artillery_network.median");
    private static final Component PRES = Component.translatable("block.cbc_ballistics.ballistic_calculator.precision");
    private static final Component TTT = Component.translatable("block.cbc_ballistics.ballistic_calculator.travelTime");
    private static final Component DISP = Component.translatable("block.cbc_ballistics.ballistic_calculator.dispersion");
    private static final Component BLOCKS = Component.translatable("block.cbc_ballistics.ballistic_calculator.blocks");
    private static final Component SECONDS = Component.translatable("block.cbc_ballistics.ballistic_calculator.seconds");
    private static final Component SUPER = Component.translatable("unit.cbc_ballistics.artillery_network.super_short");
    private static final Component SUB = Component.translatable("unit.cbc_ballistics.artillery_network.sub_short");

    private int ticks;

    private int leftBound;
    private int topBound;

    private final int imageHeight;
    private final int imageWidth;
    private Artillery_CoordinatorInterface data;
    private final BlockPos pos;

    private EditBox netId;
    private EditBox targetPos;
    private boolean targetError;
    private boolean changedTarget;

    private int changeCooldown;
    private TextAndImageButton sendButton;
    private int sendCooldown;
    private TextAndImageButton setButton;
    private int setCooldown;
    private TextAndImageButton modeButton;
    private int modeCooldown;
    private TextAndImageButton fireButton;
    private int fireCooldown;

    private double medianPres;
    private double medianDisp;
    private double medianTTT;


    public Artillery_CoordinatorScreen(BlockPos pos, Artillery_CoordinatorInterface data) {
        super(Component.literal("Screen"));
        this.imageWidth = 96 * 2;
        this.imageHeight = 54 * 2;

        this.data = data;
        this.pos = pos;
        medianDisp = 0.0;
        medianTTT = 0.0;
        medianPres = 0.0;
    }

    @Override
    protected void init() {
        super.init();
        leftBound = (width - imageWidth) / 2;
        topBound = (height - imageHeight) / 2;
        ticks = 0;

        int x = imageWidth / 2;
        int y = (int)(imageHeight * 0.75);

        //add widgets here
        targetPos = addRenderableWidget(new EditBox(FONT, leftBound + 8, topBound + 14, 176, 8, TARGET_POS));
        targetPos.setMaxLength(Integer.MAX_VALUE);
        targetPos.setSuggestion("_");
        targetPos.setBordered(false);
        targetPos.setValue(data.getTargetPos());

        if(data.allowIdChange()) {
            netId = addRenderableWidget(new EditBox(FONT, leftBound + 32, topBound + 66, 176, 8, TARGET_POS));
            netId.setMaxLength(5);
            netId.setSuggestion("_");
            netId.setBordered(false);
            netId.setValue(data.getNetworkId());
        }

        sendButton = addRenderableWidget(TextAndImageButton.builder(SEND, getButton(sendCooldown),this::send).build());
        sendButton.setPosition(leftBound + 8,topBound + 26);
        sendButton.setHeight(16);
        sendButton.setWidth(56);

        setButton = addRenderableWidget(TextAndImageButton.builder(SET, getButton(setCooldown),this::set).build());
        setButton.setPosition(leftBound + 68,topBound + 26);
        setButton.setHeight(16);
        setButton.setWidth(56);

        modeButton = addRenderableWidget(TextAndImageButton.builder(getMode(), getButton(modeCooldown),this::mode).build());
        modeButton.setPosition(leftBound + 128,topBound + 26);
        modeButton.setHeight(16);
        modeButton.setWidth(56);

        fireButton = addRenderableWidget(TextAndImageButton.builder(FIRE, getButton(fireCooldown),this::fire).build());
        fireButton.setPosition(leftBound + 128,topBound + 66);
        fireButton.setHeight(16);
        fireButton.setWidth(56);

        check();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        pGuiGraphics.blit(BACKGROUND, leftBound, topBound, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        //add renderables here

        if(targetError) {
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 6, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 6, 16777215, false);
        }

        if(targetPos.getValue().equals(data.getTargetPos())) {
            sendButton.renderString(pGuiGraphics, FONT, 16777215);
        } else {
            sendButton.renderString(pGuiGraphics, FONT, 16007990);
        }
        sendButton.renderTexture(pGuiGraphics, getButton(sendCooldown),leftBound + 8,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        if(!changedTarget) {
            setButton.renderString(pGuiGraphics, FONT, 16777215);
        } else {
            setButton.renderString(pGuiGraphics, FONT, 16007990);
        }
        setButton.renderTexture(pGuiGraphics, getButton(setCooldown),leftBound + 68,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        modeButton.setMessage(getMode());
        modeButton.renderString(pGuiGraphics, FONT, 16777215);
        modeButton.renderTexture(pGuiGraphics, getButton(modeCooldown),leftBound + 128,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        fireButton.renderTexture(pGuiGraphics, getButton(fireCooldown),leftBound + 128,topBound + 66, 0, 0, 0, 56, 16, 56, 16 );

        if(data.allowIdChange()) {
            pGuiGraphics.drawString(FONT, ID.getString(), leftBound + 8, topBound + 66, 16777215, false);
        } else {
            pGuiGraphics.drawString(FONT, ID.getString() + data.getNetworkId(), leftBound + 8, topBound + 66, 16777215, false);
        }
        pGuiGraphics.drawString(FONT, NUM_CAN.getString() + ("" + data.getNumCannons()), leftBound + 68, topBound + 66, 16777215, false);
        pGuiGraphics.drawString(FONT, NUM_DIR.getString() + ("" + data.getNumDirector()), leftBound + 8, topBound + 74, 16777215, false);
        pGuiGraphics.drawString(FONT, READY_CAN.getString() + ("" + data.getNumReadyCannons()), leftBound + 68, topBound + 74, 16777215, false);

        float[] medianSet = data.getMedianSet();
        pGuiGraphics.drawString(FONT, MEDIAN, leftBound + 8, topBound + 46, 16777215, false);
        double pres = Math.round(medianSet[0] * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, PRES.getString() + pres + BLOCKS.getString(), leftBound + 8, topBound + 54, 16777215, false);
        double disp = Math.round(medianSet[1] * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, DISP.getString() + disp + BLOCKS.getString(), leftBound + 98, topBound + 46, 16777215, false);
        double tTT = Math.round((medianSet[2] / 20) * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, TTT.getString() + tTT + SECONDS.getString(), leftBound + 98, topBound + 54, 16777215, false);

        pGuiGraphics.drawString(FONT, SUPER, leftBound + 8, topBound + 86, 16777215, false);
        pGuiGraphics.drawString(FONT, data.getSuperiorId(), leftBound + 8, topBound + 94, 16777215, false);
        pGuiGraphics.drawString(FONT, SUB, leftBound + 98, topBound + 86, 16777215, false);
        pGuiGraphics.drawString(FONT, "" + data.getNumSubnets(), leftBound + 98, topBound + 94, 16777215, false);
    }





    private void check() {
            changeCooldown = 5;
            if (targetPos.getValue().isEmpty()) {
                targetError = false;
            } else {
                targetError = !Utils.posFromString(targetPos.getValue(), new int[3]);
            }
    }

    public void tick() {
        ticks++;
        setCooldown = Math.max(0, setCooldown - 1);
        sendCooldown = Math.max(0, sendCooldown - 1);
        modeCooldown = Math.max(0, modeCooldown - 1);
        fireCooldown = Math.max(0, fireCooldown - 1);
        if(ticks == 20) {
            ticks = 0;
            check();
        }
        if (data.allowIdChange()) {
            String value = netId.getValue();
            if (!data.getNetworkId().equals(value)) {
                if(value.length() == 5) {
                    netId.setTextColor(16777215);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("network_id", data.getNetworkId());
                    tag.putString("new_network_id", value);
                    data.update(tag);
                    setNetId();
                } else {
                    netId.setTextColor(16007990);
                }
            }
        }
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component getMode() {
        return data.getMode().getComponent();
    }

    public void send(Button button) {
        if(sendCooldown <= 0 && !targetError) {
            changedTarget = true;
            sendCooldown = 5;
            data.setTargetPos(targetPos.getValue());
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(pos, (byte) 0, targetPos.getValue()));
        }
    }

    public void set(Button button) {
        if(setCooldown <= 0 && !targetError) {
            changedTarget = false;
            setCooldown = 5;
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(pos, (byte)1, "nothing"));
        }
    }

    public void mode(Button button) {
        if(modeCooldown <= 0 && !targetError) {
            modeCooldown = 5;
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(pos, (byte) 2,"" + data.getMode().next().NUM));
            send(null);
        }
    }

    public void fire(Button button) {
        if(fireCooldown <= 0 && !targetError) {
            fireCooldown = 5;
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(pos, (byte)3, "nothing"));
        }
    }

    public void setNetId() {
        ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(pos, (byte) 4, netId.getValue()));
    }

    private ResourceLocation getButton(int ticks) {
        if(ticks <= 0) {
            return new ResourceLocation(CBC_Ballistics.MODID, "textures/gui/artillery_network/28x8button_up.png");
        } else {
            return new ResourceLocation(CBC_Ballistics.MODID, "textures/gui/artillery_network/28x8button_down.png");
        }
    }
}
