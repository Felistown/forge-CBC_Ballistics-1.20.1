package net.felis.cbc_ballistics.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.felis.cbc_ballistics.block.custom.CannonControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlock;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public class CannonControllerBlockEntity extends KineticBlockEntity {

    private float targetPitch;
    private float targetYaw;
    private CannonMountBlockEntity cannon;


    public CannonControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void onPlace(Level level) {
        cannon = findCannon(level);
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if(cannon == null && !pLevel.isClientSide) {
            cannon = findCannon(pLevel);
        }
    }

    @Override
    public void tick() {
        super.tick();
        getPersistentData().putFloat("spd", getSpeed());
        rotateCannon();
    }

    private void rotateCannon() {
        if(cannon != null && level != null && !level.isClientSide) {
            PitchOrientedContraptionEntity mount = cannon.getContraption();
            if(mount != null) {
                if(mount.pitch != targetPitch) {
                    cannon.setPitch(targetPitch);
                    cannon.notifyUpdate();
                }
                if(mount.yaw != targetYaw) {
                    cannon.setPitch(targetYaw);
                    cannon.notifyUpdate();
                }

                getPersistentData().putFloat("pitch", mount.pitch);
                getPersistentData().putFloat("yaw", mount.yaw);

            }
        }
    }

    public boolean fire() {
        try {
            cannon.getLevel().setBlock(cannon.getBlockPos(), (BlockState) cannon.getBlockState().setValue(CannonMountBlock.FIRE_POWERED, true), 3);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setTargetPitch(float targetPitch) {
        this.targetPitch = targetPitch;
        getPersistentData().putFloat("tpit", targetPitch);
    }

    public void setTargetYaw(float targetYaw) {
        this.targetYaw = targetYaw;
        getPersistentData().putFloat("tyaw", targetYaw);
    }

    private CannonMountBlockEntity findCannon(Level level) {
        BlockEntity block = level.getBlockEntity(worldPosition.east());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        }
        block = level.getBlockEntity(worldPosition.south());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        }
        block = level.getBlockEntity(worldPosition.west());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        }
        block = level.getBlockEntity(worldPosition.north());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        }
        block = level.getBlockEntity(worldPosition.above());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        }
        block = level.getBlockEntity(worldPosition.below());
        if(block instanceof CannonMountBlockEntity) {
            return (CannonMountBlockEntity)block;
        } else {
            return null;
        }
    }
}
