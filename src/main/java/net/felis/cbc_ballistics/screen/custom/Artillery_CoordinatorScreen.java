package net.felis.cbc_ballistics.screen.custom;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SendArtilleryNetworkInstructionC2SPacket;
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
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private static final Component DIRECT = Component.translatable("block.cbc_ballistics.ballistic_calculator.direct");
    private static final Component INDIRECT = Component.translatable("block.cbc_ballistics.ballistic_calculator.indirect");
    private static final Component PRECISE= Component.translatable("block.cbc_ballistics.artillery_network.pres");
    private static final Component DISPERSION = Component.translatable("block.cbc_ballistics.artillery_network.dis");
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
    private ArtilleryCoordinatorBlockEntity block;
    private final BlockPos pos;

    private EditBox targetPos;
    private boolean targetError;

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


    public Artillery_CoordinatorScreen(BlockPos pos) {
        super(Component.literal("Screen"));
        this.imageWidth = 96 * 2;
        this.imageHeight = 54 * 2;
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
        if (minecraft != null && minecraft.level != null) {
            BlockEntity block = minecraft.level.getBlockEntity(pos);
            if (block instanceof ArtilleryCoordinatorBlockEntity) {
                this.block = (ArtilleryCoordinatorBlockEntity) block;
            }
        }

        int x = imageWidth / 2;
        int y = (int)(imageHeight * 0.75);
        CompoundTag tag = block.getPersistentData();

        //add widgets here
        targetPos = addRenderableWidget(new EditBox(FONT, leftBound + 8, topBound + 14, 176, 8, TARGET_POS));
        targetPos.setMaxLength(Integer.MAX_VALUE);
        targetPos.setSuggestion("_");
        targetPos.setBordered(false);
        targetPos.setValue(tag.getString("targetPos"));

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
        CompoundTag tag = block.getPersistentData();
        //add renderables here

        if(targetError) {
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 6, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 6, 16777215, false);
        }
        int[] array = new int[3];
        Utils.posFromString(tag.getString("targetPos"), array);
        if(!block.changedTarget()) {
            sendButton.renderString(pGuiGraphics, FONT, 16777215);
        } else {
            sendButton.renderString(pGuiGraphics, FONT, 16007990);
        }
        sendButton.renderTexture(pGuiGraphics, getButton(sendCooldown),leftBound + 8,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        if(block.allSet()) {
            setButton.renderString(pGuiGraphics, FONT, 16777215);
        } else {
            setButton.renderString(pGuiGraphics, FONT, 16007990);
        }
        setButton.renderTexture(pGuiGraphics, getButton(setCooldown),leftBound + 68,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        modeButton.setMessage(getMode());
        modeButton.renderString(pGuiGraphics, FONT, 16777215);
        modeButton.renderTexture(pGuiGraphics, getButton(modeCooldown),leftBound + 128,topBound + 26, 0, 0, 0, 56, 16, 56, 16 );

        fireButton.renderTexture(pGuiGraphics, getButton(fireCooldown),leftBound + 128,topBound + 66, 0, 0, 0, 56, 16, 56, 16 );

        pGuiGraphics.drawString(FONT, ID.getString() + block.getNetwork_id(), leftBound + 8, topBound + 66, 16777215, false);
        pGuiGraphics.drawString(FONT, NUM_CAN.getString() + ("" + block.getAllCannons().size()), leftBound + 68, topBound + 66, 16777215, false);
        pGuiGraphics.drawString(FONT, NUM_DIR.getString() + ("" + block.getAllDirectors().size()), leftBound + 8, topBound + 74, 16777215, false);
        pGuiGraphics.drawString(FONT, READY_CAN.getString() + ("" + tag.getInt("readies")), leftBound + 68, topBound + 74, 16777215, false);

        pGuiGraphics.drawString(FONT, MEDIAN, leftBound + 8, topBound + 46, 16777215, false);
        double pres = Math.round(block.getMedianPres() * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, PRES.getString() + pres + BLOCKS.getString(), leftBound + 8, topBound + 54, 16777215, false);
        double disp = Math.round(block.getMedianDisp() * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, DISP.getString() + disp + BLOCKS.getString(), leftBound + 98, topBound + 46, 16777215, false);
        double tTT = Math.round((block.getMedianTTT()/ 20) * 1000) / 1000.0;
        pGuiGraphics.drawString(FONT, TTT.getString() + tTT + SECONDS.getString(), leftBound + 98, topBound + 54, 16777215, false);

        pGuiGraphics.drawString(FONT, SUPER, leftBound + 8, topBound + 86, 16777215, false);
        ArtilleryCoordinatorBlockEntity be = block.getSuperior();
        if(be != null && be.getNetwork_id() != null) {
            pGuiGraphics.drawString(FONT, be.getNetwork_id(), leftBound + 8, topBound + 94, 16777215, false);
        }
        pGuiGraphics.drawString(FONT, SUB, leftBound + 98, topBound + 86, 16777215, false);
        pGuiGraphics.drawString(FONT, "" + block.getSuboridinates().size(), leftBound + 98, topBound + 94, 16777215, false);
    }





    private void check() {
        if(targetPos.getValue().isEmpty()) {
            targetError = false;
        } else {
            targetError = !block.setTargetPos(targetPos.getValue());
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
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component getMode() {
        switch (block.getMode()) {
            case 0:
                return INDIRECT;
            case 1:
                return DIRECT;
            case 2:
                return PRECISE;
            case 3:
                return DISPERSION;
            default:
                return Component.empty();
        }
    }

    public void send(Button button) {
        if(sendCooldown <= 0 && !targetError) {
            sendCooldown = 5;
            if(block.getTargetPos() != null) {
                CompoundTag tag = block.getPersistentData();
                ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(block.getBlockPos(), (byte) 0, tag.getString("targetPos")));
                block.setTarget(tag.getString("targetPos"));
            }
        }
    }

    public void set(Button button) {
        if(setCooldown <= 0 && !targetError) {
            setCooldown = 5;
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(block.getBlockPos(), (byte)1, "nothing"));
            block.target();
        }
    }

    public void mode(Button button) {
        if(modeCooldown <= 0 && !targetError) {
            modeCooldown = 5;
            block.changeMode();
            send(null);
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(block.getBlockPos(), (byte) 2,"" + block.getMode()));
        }
    }

    public void fire(Button button) {
        if(fireCooldown <= 0 && !targetError) {
            fireCooldown = 5;
            ModMessages.sendToServer(new SendArtilleryNetworkInstructionC2SPacket(block.getBlockPos(), (byte)3, "nothing"));
        }
    }

    private ResourceLocation getButton(int ticks) {
        if(ticks <= 0) {
            return new ResourceLocation(CBC_Ballistics.MODID, "textures/gui/artillery_network/28x8button_up.png");
        } else {
            return new ResourceLocation(CBC_Ballistics.MODID, "textures/gui/artillery_network/28x8button_down.png");
        }
    }
}
