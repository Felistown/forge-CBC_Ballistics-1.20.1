package net.felis.cbc_ballistics.screen.custom;

import net.felis.cbc_ballistics.CBS_Ballistics;
import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SyncCalculatorC2SPacket;
import net.felis.cbc_ballistics.util.calculator.Projectile;
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


public class Ballistic_CalculatorScreen extends Screen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(CBS_Ballistics.MODID, "textures/gui/calculator/calculator_interface.png");
    private static final Component CANNON_POS = Component.translatable("block.cbc_ballistics.ballistic_calculator.cannonPos");
    private static final Component TARGET_POS = Component.translatable("block.cbc_ballistics.ballistic_calculator.targetPos");
    private static final Component MIN_PITCH = Component.translatable("block.cbc_ballistics.ballistic_calculator.minPitch");
    private static final Component MAX_PITCH = Component.translatable("block.cbc_ballistics.ballistic_calculator.maxPitch");
    private static final Component LENGTH = Component.translatable("block.cbc_ballistics.ballistic_calculator.length");
    private static final Component GRAV = Component.translatable("block.cbc_ballistics.ballistic_calculator.grav");
    private static final Component DRAG = Component.translatable("block.cbc_ballistics.ballistic_calculator.drag");
    private static final Component CHARGES = Component.translatable("block.cbc_ballistics.ballistic_calculator.charge");
    private static final Component MIN_CHARGE = Component.translatable("block.cbc_ballistics.ballistic_calculator.minCharge");
    private static final Component MAX_CHARGE = Component.translatable("block.cbc_ballistics.ballistic_calculator.maxCharge");
    private static final Component CALCULATE = Component.translatable("block.cbc_ballistics.ballistic_calculator.calculate");
    private static final Component OUT_OF_RANGE = Component.translatable("block.cbc_ballistics.ballistic_calculator.outOfRange");
    private static final Component PITCH = Component.translatable("block.cbc_ballistics.ballistic_calculator.pitch");
    private static final Component PRECISION = Component.translatable("block.cbc_ballistics.ballistic_calculator.precision");
    private static final Component DISPERSION = Component.translatable("block.cbc_ballistics.ballistic_calculator.dispersion");
    private static final Component YAW = Component.translatable("block.cbc_ballistics.ballistic_calculator.yaw");
    private static final Component TRAVEL_TIME = Component.translatable("block.cbc_ballistics.ballistic_calculator.travelTime");
    private static final Component BLOCKS = Component.translatable("block.cbc_ballistics.ballistic_calculator.blocks");
    private static final Component SECONDS = Component.translatable("block.cbc_ballistics.ballistic_calculator.seconds");
    private static final Font FONT = Minecraft.getInstance().font;

    private int ticks;

    private int leftBound;
    private int topBound;

    private final int imageHeight;
    private final int imageWidth;
    private CalculatorBlockEntity block;
    private final BlockPos pos;

    private EditBox cannonPos;
    private boolean cannonError;
    private EditBox targetPos;
    private boolean targetError;
    private EditBox minPitch;
    private boolean minPitchError;
    private EditBox maxPitch;
    private boolean maxPitchError;
    private EditBox length;
    private boolean lengthError;
    private EditBox maxCharge;
    private boolean maxChargeError;
    private EditBox minCharge;
    private boolean minChargeError;
    private EditBox grav;
    private boolean gravError;
    private EditBox drag;
    private boolean dragError;
    private TextAndImageButton material;
    private int materialCooldown;
    private TextAndImageButton modeButton;
    private int modeCooldown;
    private TextAndImageButton calculateButton;
    private int calculateCooldown;
    private boolean allowPress;


    public Ballistic_CalculatorScreen(BlockPos pos) {
        super(Component.literal("Screen"));
        this.imageWidth = 96 * 2;
        this.imageHeight = 64 * 2;
        this.pos = pos;
        modeCooldown = 0;
        allowPress = true;
        calculateCooldown = 0;
        materialCooldown = 0;
    }

    @Override
    protected void init() {
        super.init();
        leftBound = (width - imageWidth) / 2;
        topBound = (height - imageHeight) / 2;
        ticks = 0;
        if(minecraft != null && minecraft.level != null) {
            BlockEntity block = minecraft.level.getBlockEntity(pos);
            if(block instanceof CalculatorBlockEntity) {
                this.block = (CalculatorBlockEntity)block;
            }
        }
        
        // add widgets here
        int x = imageWidth / 2;
        int y = (int)(imageHeight * 0.75);
        CompoundTag tag = block.getPersistentData();

        //Cannon pos widget
        cannonPos = addRenderableWidget(new EditBox(FONT, leftBound + 8, topBound + 14, 176, 8, CANNON_POS));
        cannonPos.setMaxLength(Integer.MAX_VALUE);
        cannonPos.setSuggestion("_");
        cannonPos.setBordered(false);
        cannonPos.setValue(tag.getString("cannonPos"));

         //Target pos widget
        targetPos = addRenderableWidget(new EditBox(FONT, leftBound + 8, topBound + 54, 176, 8, TARGET_POS));
        targetPos.setMaxLength(Integer.MAX_VALUE);
        targetPos.setSuggestion("_");
        targetPos.setBordered(false);
        targetPos.setValue(tag.getString("targetPos"));

        //min pitch widget
        minPitch = addRenderableWidget(new EditBox(FONT, leftBound + 8, topBound + 34, 30, 8, MIN_PITCH));
        minPitch.setMaxLength(4);
        minPitch.setSuggestion("°");
        minPitch.setBordered(false);
        minPitch.setValue(tag.getString("minPitch"));

        //max pitch widget
        maxPitch = addRenderableWidget(new EditBox(FONT, leftBound + 42, topBound + 34, 30, 8, MAX_PITCH));
        maxPitch.setMaxLength(4);
        maxPitch.setSuggestion("°");
        maxPitch.setBordered(false);
        maxPitch.setValue(tag.getString("maxPitch"));

        //length widget
        length = addRenderableWidget(new EditBox(FONT, leftBound + 76, topBound + 34, 30, 8, LENGTH));
        length.setMaxLength(5);
        length.setSuggestion("_");
        length.setBordered(false);
        length.setValue(tag.getString("length"));

        //gravity widget
        grav = addRenderableWidget(new EditBox(FONT, leftBound + 76, topBound + 74, 30, 8, GRAV));
        grav.setMaxLength(5);
        grav.setSuggestion("_");
        grav.setBordered(false);
        grav.setValue(tag.getString("gravity"));

        //drag widget
        drag = addRenderableWidget(new EditBox(FONT, leftBound + 110, topBound + 74, 30, 8, DRAG));
        drag.setMaxLength(5);
        drag.setSuggestion("_");
        drag.setBordered(false);
        drag.setValue(tag.getString("drag"));

        //min charge widget
        minCharge = addRenderableWidget(new EditBox(FONT, leftBound + 130, topBound + 34, 20, 8, MIN_CHARGE));
        minCharge.setMaxLength(2);
        minCharge.setSuggestion("_");
        minCharge.setBordered(false);
        minCharge.setValue(tag.getString("minCharge"));

        //max charge widget
        maxCharge = addRenderableWidget(new EditBox(FONT, leftBound + 170, topBound + 34, 16, 8, MAX_CHARGE));
        maxCharge.setMaxLength(2);
        maxCharge.setSuggestion("_");
        maxCharge.setBordered(false);
        maxCharge.setValue(tag.getString("maxCharge"));

        //material button
        material = addRenderableWidget(TextAndImageButton.builder(block.getMaterial(), getMaterialButton(),this::cycleMaterial).build());
        material.setPosition(leftBound + 144,topBound + 66);
        material.setHeight(16);
        material.setWidth(40);

        //mode button
        modeButton = addRenderableWidget(TextAndImageButton.builder(block.getMode(), getModeButton(),this::mode).build());
        modeButton.setPosition(leftBound + 8,topBound + 86);
        modeButton.setHeight(16);
        modeButton.setWidth(64);

        //calculate button
        calculateButton = addRenderableWidget(TextAndImageButton.builder(CALCULATE, getCalculatebutton(),this::calculate).build());
        calculateButton.setPosition(leftBound + 8,topBound + 66);
        calculateButton.setHeight(16);
        calculateButton.setWidth(64);
        calculateButton.setMessage(CALCULATE);

        check();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        pGuiGraphics.blit(BACKGROUND, leftBound, topBound, 0, 0, imageWidth ,imageHeight, imageWidth, imageHeight);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        boolean error = false;
        if(cannonError) {
            error = true;
            pGuiGraphics.drawString(FONT, CANNON_POS, leftBound + 8, topBound + 6, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, CANNON_POS, leftBound + 8, topBound + 6, 16777215, false);
        }

        if(targetError) {
            error = true;
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 46, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, TARGET_POS, leftBound + 8, topBound + 46, 16777215, false);
        }

        if(minPitchError) {
            error = true;
            pGuiGraphics.drawString(FONT, MIN_PITCH, leftBound + 8, topBound + 26, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, MIN_PITCH, leftBound + 8, topBound + 26, 16777215, false);
        }

        if(maxPitchError) {
            error = true;
            pGuiGraphics.drawString(FONT, MAX_PITCH, leftBound + 42, topBound + 26, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, MAX_PITCH, leftBound + 42, topBound + 26, 16777215, false);
        }

        if(lengthError) {
            error = true;
            pGuiGraphics.drawString(FONT, LENGTH, leftBound + 76, topBound + 26, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, LENGTH, leftBound + 76, topBound + 26, 16777215, false);
        }

        if(gravError) {
            error = true;
            pGuiGraphics.drawString(FONT, GRAV, leftBound + 76, topBound + 66, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, GRAV, leftBound + 76, topBound + 66, 16777215, false);
        }

        if(dragError) {
            error = true;
            pGuiGraphics.drawString(FONT, DRAG, leftBound + 110, topBound + 66, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, DRAG, leftBound + 110, topBound + 66, 16777215, false);
        }

        pGuiGraphics.drawString(FONT, CHARGES, leftBound + 114, topBound + 26, 16777215, false);

        if(minChargeError) {
            error = true;
            pGuiGraphics.drawString(FONT, MIN_CHARGE, leftBound + 114, topBound + 34, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, MIN_CHARGE, leftBound + 114, topBound + 34, 16777215, false);
        }

        if(maxChargeError) {
            error = true;
            pGuiGraphics.drawString(FONT, MAX_CHARGE, leftBound + 150, topBound + 34, 16007990, false);
        } else {
            pGuiGraphics.drawString(FONT, MAX_CHARGE, leftBound + 150, topBound + 34, 16777215, false);
        }

        material.setMessage(block.getMaterial());
        material.renderString(pGuiGraphics, FONT, 16777215);
        material.renderTexture(pGuiGraphics, getMaterialButton(), leftBound + 144,topBound + 66, 0, 0, 0, 40, 16, 40, 16 );

        modeButton.setMessage(block.getMode());
        modeButton.renderString(pGuiGraphics, FONT, 16777215);
        modeButton.renderTexture(pGuiGraphics, getModeButton(), leftBound + 8,topBound + 86, 0, 0, 0, 64, 16, 64, 16 );

        if(error) {
            allowPress = false;
            calculateButton.renderString(pGuiGraphics, FONT, 16007990);
        } else {
            allowPress = true;
            calculateButton.renderString(pGuiGraphics, FONT, 16777215);
        }
        calculateButton.renderTexture(pGuiGraphics, getCalculatebutton(), leftBound + 8,topBound + 66, 0, 0, 0, 64, 16, 64, 16 );

        if(block.isReady()) {
            if(block.isTooFar()) {
                pGuiGraphics.drawString(FONT, OUT_OF_RANGE, leftBound + 76, topBound + 86, 16007990);
            } else if(block.getResult() != null){
                Projectile projectile = block.getResult();
                double pitch = Math.round(projectile.getPitch() * 1000) / 1000.0;
                pGuiGraphics.drawString(FONT, PITCH.getString() + pitch, leftBound + 76, topBound + 86, 16777215, false);
                pGuiGraphics.drawString(FONT, CHARGES.getString() + ": " + projectile.getCharges(), leftBound + 76, topBound + 94, 16777215, false);
                double yaw = Math.round(projectile.getCannon().getYaw() * 1000) / 1000.0;
                pGuiGraphics.drawString(FONT, YAW.getString() + yaw, leftBound + 8, topBound + 106, 16777215);
                double pres = Math.round(projectile.getPrecision() * 1000) / 1000.0;
                pGuiGraphics.drawString(FONT, PRECISION.getString() + pres + BLOCKS.getString(), leftBound + 8, topBound + 114, 16777215, false);
                double disp = Math.round(projectile.getDispersion() * 1000) / 1000.0;
                pGuiGraphics.drawString(FONT, DISPERSION.getString() + disp + BLOCKS.getString(), leftBound + 98, topBound + 106, 16777215, false);
                double TTT = Math.round((projectile.getAirtime() / 20) * 1000) / 1000.0;
                pGuiGraphics.drawString(FONT, TRAVEL_TIME.getString() + TTT + SECONDS.getString(), leftBound + 98, topBound + 114, 16777215, false);
            }
        }
    }

    @Override
    public void tick() {
        ticks++;
        materialCooldown = Math.max(0, materialCooldown - 1);
        modeCooldown = Math.max(0, modeCooldown - 1);
        calculateCooldown = Math.max(0, calculateCooldown - 1);
        if(ticks == 20) {
            ticks = 0;
            check();
        }
        super.tick();
    }

    private void check() {
        if(cannonPos.getValue().isEmpty()) {
            cannonError = false;
        } else {
            cannonError = !block.setCannonPos(cannonPos.getValue());
        }

        if(targetPos.getValue().isEmpty()) {
            targetError = false;
        } else {
            targetError = !block.setTargetPos(targetPos.getValue());
        }

        if(minPitch.getValue().isEmpty()) {
            minPitchError = false;
        } else {
            minPitchError = !block.setMinPitch(minPitch.getValue());
        }

        if(maxPitch.getValue().isEmpty()) {
            maxPitchError = false;
        } else {
            maxPitchError = !block.setMaxPitch(maxPitch.getValue());
        }

        if(length.getValue().isEmpty()) {
            lengthError = false;
        } else {
            lengthError = !block.setLength(length.getValue());
        }

        if(grav.getValue().isEmpty()) {
            gravError = false;
        } else {
            gravError = !block.setGravity(grav.getValue());
        }

        if(drag.getValue().isEmpty()) {
            dragError = false;
        } else {
            dragError = !block.setDrag(drag.getValue());
        }


        if(maxCharge.getValue().isEmpty()) {
            maxChargeError = false;
        } else {
            maxChargeError = !block.setMaxCharge(maxCharge.getValue());
        }


        if(minCharge.getValue().isEmpty()) {
            minChargeError = false;
        } else {
            minChargeError = !block.setMinCharge(minCharge.getValue());
        }
    }

    private ResourceLocation getMaterialButton() {
        if(materialCooldown <= 0) {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/20x8button_up.png");
        } else {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/20x8button_down.png");
        }
    }

    private ResourceLocation getModeButton() {
        if(modeCooldown <= 0) {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/38x8button_up.png");
        } else {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/38x8button_down.png");
        }
    }

    private ResourceLocation getCalculatebutton() {
        if(calculateCooldown <= 0) {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/38x8button_up.png");
        } else {
            return new ResourceLocation(CBS_Ballistics.MODID,"textures/gui/calculator/38x8button_down.png");
        }
    }

    private void cycleMaterial(Button button) {
        if(materialCooldown <= 0) {
            materialCooldown = 5;
            block.cycleMaterial();
        }
    }

    private void mode(Button button) {
        if(modeCooldown <= 0) {
            modeCooldown = 5;
            block.cycleMode();
        }
    }

    private void calculate(Button button) {
        if(calculateCooldown <= 0 && allowPress) {
            calculateCooldown = 5;
            allowPress = false;
            block.calculate(this);
        }
    }

    public void setAllowPress() {
        allowPress = true;
    }

    @Override
    public void onClose() {
        block.setCannonPos(cannonPos.getValue());
        block.setTargetPos(targetPos.getValue());
        block.setMinPitch(minPitch.getValue());
        block.setMaxPitch(maxPitch.getValue());
        block.setLength(length.getValue());
        block.setGravity(grav.getValue());
        block.setDrag(drag.getValue());
        block.setMaxPitch(maxPitch.getValue());
        block.setMinPitch(minPitch.getValue());
        ModMessages.sendToServer(new SyncCalculatorC2SPacket(block.getBlockPos(), block.getPersistentData()));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
