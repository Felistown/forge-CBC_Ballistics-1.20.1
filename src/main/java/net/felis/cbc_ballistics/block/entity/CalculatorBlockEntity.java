package net.felis.cbc_ballistics.block.entity;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import net.felis.cbc_ballistics.block.ModBlocks;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SyncCalculatorC2SPacket;
import net.felis.cbc_ballistics.networking.packet.SyncCalculatorS2CPacket;
import net.felis.cbc_ballistics.screen.custom.Ballistic_CalculatorScreen;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.felis.cbc_ballistics.util.calculator.Cannon;
import net.felis.cbc_ballistics.util.calculator.Projectile;
import net.felis.cbc_ballistics.util.calculator.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CalculatorBlockEntity extends BlockEntity implements MenuProvider, IHaveGoggleInformation, Director {

    private static final Component CAST_IRON = Component.translatable("block.cbc_ballistics.ballistic_calculator.castIron");
    private static final Component STEEL = Component.translatable("block.cbc_ballistics.ballistic_calculator.steel");
    private static final Component BRONZE = Component.translatable("block.cbc_ballistics.ballistic_calculator.bronze");
    private static final Component NETHER_STEEL = Component.translatable("block.cbc_ballistics.ballistic_calculator.netherSteel");
    private static final Component WROUGHT_IRON = Component.translatable("block.cbc_ballistics.ballistic_calculator.wroughtIron");
    private static final Component CHARGES = Component.translatable("block.cbc_ballistics.ballistic_calculator.charge");
    private static final Component PITCH = Component.translatable("block.cbc_ballistics.ballistic_calculator.pitch");
    private static final Component YAW = Component.translatable("block.cbc_ballistics.ballistic_calculator.yaw");
    private static final Component OUT_OF_RANGE = Component.translatable("block.cbc_ballistics.ballistic_calculator.outOfRange");

    private boolean calculate;
    private boolean isDirectFire;

    private int material;

    private int[] cPos;
    private int[] tPos;
    private int minP;
    private int maxP;
    private int len;
    private int minC;
    private int maxC;
    private double grav;
    private double drg;

    private Projectile[] results;
    private boolean ready;
    private boolean tooFar;

    private ArtilleryCoordinatorBlockEntity network;
    private int id;

    private byte redstonePulse;


    public CalculatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CALCULATORBLOCKENTITY.get(), pPos, pBlockState);
        cPos = new int[3];
        tPos = new int[3];
        calculate = false;
        isDirectFire = false;
        ready = false;
        redstonePulse = -1;
        CompoundTag pTag = getPersistentData();
        pTag.putString("cannonPos", "");
        pTag.putString("targetPos", "");
        pTag.putString("maxPitch", "60");
        pTag.putString("minPitch", "-30");
        pTag.putString("length", "");
        pTag.putString("minCharge", "");
        pTag.putString("maxCharge", "");
        pTag.putInt("material", 3);
        pTag.putString("gravity", "0.05");
        pTag.putString("drag", "0.99");
        material = 3;
        minP = -30;
        maxP = 60;
        grav = 0.05;
        drg = 0.99;
        minC = 1;
        maxC = 1;
        tooFar = false;
    }

    public boolean calculate(Ballistic_CalculatorScreen screen) {
        Cannon myCannon = new Cannon(cPos[0], cPos[1], cPos[2], len, minP, maxP, getMaterialString());
        myCannon.setDrag(drg);
        myCannon.setGravity(grav);
        Target myTarget = new Target(tPos[0], tPos[1], tPos[2], myCannon);
        myCannon.setTarget(myTarget);
        ready = true;
        try {
            results = myCannon.interpolateTarget(minC, maxC);
            tooFar = false;
            if (screen != null) {
                screen.setAllowPress();
            }
            return true;
        } catch (RuntimeException e) {
            tooFar = true;
            if (screen != null) {
                screen.setAllowPress();
            }
            return false;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public Projectile getResult() {
        if (results != null) {
            if (isDirectFire) {
                return results[0];
            } else {
                return results[1];
            }
        }
        return null;
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbc_ballistics.ballistic_calculator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }

    public Component getMaterial() {
        switch (material) {
            case 0:
                return CAST_IRON;
            case 1:
                return WROUGHT_IRON;
            case 2:
                return BRONZE;
            case 3:
                return STEEL;
            case 4:
                return NETHER_STEEL;
            default:
                material = 0;
                return Component.literal("Unknown material");
        }
    }


    public boolean getCalculateButton() {
        return calculate;
    }

    public boolean isDirectFire() {
        return isDirectFire;
    }

    public boolean setCannonPos(String cannonPos) {
        getPersistentData().putString("cannonPos", cannonPos);
        setChanged();
        int[] array = new int[3];
        boolean result = Utils.posFromString(cannonPos, array);
        if (result) {
            cPos = array;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDrag(String drag) {
        getPersistentData().putString("drag", drag);
        setChanged();
        try {
            drg = Double.parseDouble(drag);
            return drg > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean setMaxCharge(String maxCharge) {
        getPersistentData().putString("maxCharge", maxCharge);
        setChanged();
        try {
            maxC = Integer.parseInt(maxCharge);
            return maxC >= minC && maxC > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void onRemove() {
        if (network != null) {
            network.removeDirector(this);
        }
    }

    public boolean setMinCharge(String minCharge) {
        getPersistentData().putString("minCharge", minCharge);
        setChanged();
        try {
            minC = Integer.parseInt(minCharge);
            return minC <= maxC && minC > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean setGravity(String gravity) {
        getPersistentData().putString("gravity", gravity);
        setChanged();
        try {
            grav = Double.parseDouble(gravity);
            return grav > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isTooFar() {
        return tooFar;
    }

    public boolean setLength(String length) {
        getPersistentData().putString("length", length);
        setChanged();
        try {
            len = Integer.parseInt(length);
            return len > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void cycleMaterial() {
        material++;
        if (material > 4) {
            material = 0;
        }
        getPersistentData().putInt("material", material);
        setChanged();
    }

    public void cycleMode() {
        isDirectFire = !isDirectFire;
    }

    public boolean setMaxPitch(String maxPitch) {
        getPersistentData().putString("maxPitch", maxPitch);
        setChanged();
        try {
            maxP = Integer.parseInt(maxPitch);
            return maxP > minP && maxP < 90;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public String getMaterialString() {
        switch (material) {
            case 0:
                return "castiron";
            case 1:
                return "wroughtiron";
            case 2:
                return "bronze";
            case 3:
                return "steel";
            case 4:
                return "nethersteel";
            default:
                material = 0;
                return "Unknown material";
        }
    }

    public boolean setMinPitch(String minPitch) {
        getPersistentData().putString("minPitch", minPitch);
        setChanged();
        try {
            minP = Integer.parseInt(minPitch);
            return minP < maxP && minP > -90;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean setTargetPos(String targetPos) {
        getPersistentData().putString("targetPos", targetPos);
        setChanged();
        int[] array = new int[3];
        boolean result = Utils.posFromString(targetPos, array);
        if (result) {
            tPos = array;
            return true;
        } else {
            return false;
        }
    }

    public Component getMode() {
        if (isDirectFire) {
            return Component.translatable("block.cbc_ballistics.ballistic_calculator.direct");
        } else {
            return Component.translatable("block.cbc_ballistics.ballistic_calculator.indirect");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putInt("material", material);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        pTag.merge(getPersistentData());
        setChanged();
        material = pTag.getInt("material");
        setCannonPos(pTag.getString("cannonPos"));
        setTargetPos(pTag.getString("targetPos"));
        setMinPitch(pTag.getString("minPitch"));
        setMaxPitch(pTag.getString("maxPitch"));
        setLength(pTag.getString("length"));
        setGravity(pTag.getString("gravity"));
        setDrag(pTag.getString("drag"));
        setMaxCharge(pTag.getString("maxCharge"));
        setMinCharge(pTag.getString("minCharge"));
    }

    public void sync() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModMessages.sendToServer(new SyncCalculatorC2SPacket(getBlockPos(), getPersistentData()))
        );
    }

    public void syncFrom(CompoundTag pTag) {
        material = pTag.getInt("material");
        setCannonPos(pTag.getString("cannonPos"));
        setTargetPos(pTag.getString("targetPos"));
        setMinPitch(pTag.getString("minPitch"));
        setMaxPitch(pTag.getString("maxPitch"));
        setLength(pTag.getString("length"));
        setGravity(pTag.getString("gravity"));
        setDrag(pTag.getString("drag"));
        setMaxCharge(pTag.getString("maxCharge"));
        setMinCharge(pTag.getString("minCharge"));
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block.cbc_ballistics.ballistic_calculator")));
        if (tooFar) {
            tooltip.add(OUT_OF_RANGE);
        } else if (results != null) {
            double yaw = Math.round(getResult().getTarget().getYaw() * 1000) / 1000.0;
            double pitch = Math.round(getResult().getPitch() * 1000) / 1000.0;
            tooltip.add(PITCH.copy().append("" + pitch));
            tooltip.add(YAW.copy().append("" + yaw));
            tooltip.add(CHARGES.copy().append(": " + getResult().getCharges()));
        } else {
            tooltip.add(Component.translatable("block.cbc_ballistics.ballistic_calculator.tooltip.need_results"));
        }
        return IHaveGoggleInformation.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }


    @Override
    public void setTarget(int[] target) {
        getPersistentData().putString("targetPos", ("X = " + target[0] + ", Y =" + target[1] + ", Z = " + target[2]));
        setChanged();
        tPos = target;
        calculate(null);
        if (level != null && !level.isClientSide) {
            //DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> ()->
            ModMessages.sendToPlayersRad(new SyncCalculatorS2CPacket(getBlockPos(), target), Utils.targetPoint(getBlockPos(), 200, level.dimension()));
            //);
        }
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public ArtilleryCoordinatorBlockEntity getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(ArtilleryCoordinatorBlockEntity network) {
        this.network = network;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void target() {
        if (results != null) {
            Projectile projectile = getResult();
            for (Layer c : network.getLayers(this)) {
                c.setTarget((float) projectile.getPitch(), (float) projectile.getTarget().getYaw());
            }
            redstonePulse = 8;
        }
    }

    public void tick() {
        redstonePulse();
    }

    private void redstonePulse() {
        if (redstonePulse == 0) {
            redstonePulse = -1;
            level.updateNeighborsAt(getBlockPos(), ModBlocks.BALLISTIC_CALCULATOR.get());
        } else if (redstonePulse > 0) {
            redstonePulse--;
            level.updateNeighborsAt(getBlockPos(), ModBlocks.BALLISTIC_CALCULATOR.get());
        }
    }

    public int getRedstoneOutput() {
        if (redstonePulse < 1) {
            return 0;
        } else if (results != null) {
            return getResult().getCharges();
        }
        return 0;
    }

    @Override
    public void mode(int mode) {
        /*
        0 = indirect
        1 = direct
        2 = best precision
        3 = best dispersion
        */
        switch (mode) {
            case 0:
                isDirectFire = false;
                break;
            case 1:
                isDirectFire = true;
                break;
            case 2:
                if (results != null) {
                    Projectile direct = results[0];
                    Projectile indirect = results[1];
                    isDirectFire = !(direct.getPrecisionEstimate() < indirect.getPrecisionEstimate());
                }
                break;
            case 3:
                if (results != null) {
                    Projectile direct1 = results[0];
                    Projectile indirect1 = results[1];
                    isDirectFire = !(direct1.getDispersion() < indirect1.getDispersion());
                }
                break;
        }
    }

    @Override
    public void removeNetwork() {
        if (network != null) {
            network.removeDirector(this);
        }
    }


    public boolean isSet() {
        if (results != null) {
            Projectile result = getResult();
            float pitch = (float) result.getPitch();
            float yaw = (float) result.getTarget().getYaw();
            for (Layer l : network.getLayers(this)) {
                if (l instanceof CannonControllerBlockEntity c) {
                    if (c.getTargetYaw() != yaw || c.getTargetPitch() != pitch) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}



