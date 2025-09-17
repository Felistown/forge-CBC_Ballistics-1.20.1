package net.felis.cbc_ballistics.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SyncArtilleryNetComponentC2SPacket;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;


public class CannonControllerBlockEntity extends KineticBlockEntity implements  IHaveGoggleInformation, Layer {

    private static final Logger log = LoggerFactory.getLogger(CannonControllerBlockEntity.class);
    private float targetPitch;
    private float targetYaw;
    private CannonMountBlockEntity cannon;
    private boolean active;
    private boolean setting;
    private boolean fire;
    private Director director;
    private ArtilleryCoordinatorBlockEntity network;


    public CannonControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        fire = false;
    }

    public void onRemove() {
        if (network != null){
            network.removeCannon(this);
        }
    }

    public void onPlace(Level level) {
        cannon = findCannon(level);
        if(cannon != null) {
            PitchOrientedContraptionEntity mount = cannon.getContraption();
            if(mount != null) {
                targetPitch = 0f;
                targetYaw = 0f;
            }
        }
        active = false;
        setting = false;
        fire = false;
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if(cannon == null && !pLevel.isClientSide) {
            cannon = findCannon(pLevel);
            if(cannon != null) {
                PitchOrientedContraptionEntity mount = cannon.getContraption();
                if(mount != null) {
                    targetPitch = mount.pitch;
                    targetYaw = mount.yaw;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        tryFire();
        rotateCannon();
    }

    private void rotateCannon() {
        if(cannon != null && level != null && !level.isClientSide) {
            PitchOrientedContraptionEntity mount = cannon.getContraption();
            if(mount != null) {
                updateNetwork(true);
                if (setting) {

                    float speed = Math.abs(getTheoreticalSpeed() * 0.04f);
                    boolean changed = false;
                    float pitch = mount.pitch * Math.signum(mount.getInitialOrientation().getStepX() -   mount.getInitialOrientation().getStepZ());
                    if (pitch != targetPitch) {
                        changed = true;
                        float pDiff = Math.abs(targetPitch - pitch);
                        if (pDiff > speed) {
                            float movement;
                            if (pitch > targetPitch) {
                                movement = -speed;
                            } else {
                                movement = speed;
                            }
                            cannon.setPitch(pitch + movement);
                        } else {
                            cannon.setPitch(targetPitch);
                        }
                    }

                    if (mount.yaw != targetYaw) {
                        changed = true;
                        float yaw = (mount.yaw + 360) % 360;
                        float tYaw = (targetYaw + 360) % 360;
                        float yDiff = tYaw - yaw;
                        if (Math.abs(yDiff) > speed) {
                            yDiff = (yDiff + 180) % 360 - 180;
                            float movement;
                            if (yDiff > 0) {
                                movement = speed;
                            } else  {
                                movement = -speed;
                            }
                            cannon.setYaw(mount.yaw + movement);
                        } else {
                            cannon.setYaw(targetYaw);
                        }
                    }
                    if (changed) {
                        cannon.notifyUpdate();
                    } else {
                        setting = false;
                    }
                }
                return;
            }
        }
        updateNetwork(false);
    }

    @Override
    protected boolean isNoisy() {
        return false;
    }

    private void updateNetwork(boolean active) {
        if(active == this.active) {
            if (getOrCreateNetwork() != null) {
                stress += getAddedStress();
                getOrCreateNetwork().updateStressFor(this, getAddedStress());
            }
            this.active = !active;
        }
    }

    @Override
    public void setDirector(Director director) {
        this.director =director;
    }

    @Override
    public Director getDirector() {
        return director;
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

    public void setTarget(float pitch, float yaw) {
        setting = true;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
    }

    @Override
    public boolean isSet() {
        return !setting;
    }


    public void fire() {
        fire = true;
    }

    public boolean tryFire() {
        if(fire && cannon != null && cannon.isRunning() && cannon.getContraption() != null) {
            Level level = this.getLevel();
            fire = false;
            if (level instanceof ServerLevel) {
                ServerLevel slevel = (ServerLevel)level;
                PitchOrientedContraptionEntity mountedContraption = cannon.getContraption();
                ((AbstractMountedCannonContraption)mountedContraption.getContraption()).onRedstoneUpdate(slevel, mountedContraption, true, 3, cannon);
            }
            return true;
        }
        return false;
    }


    @Override
    public float calculateStressApplied() {
        if (level != null && cannon == null) {
            cannon = findCannon(level);
        }
        return getAddedStress();
    }

    public float getAddedStress() {
        if(cannon != null && cannon.getContraption() != null) {
            float cannonStress = ((AbstractMountedCannonContraption)cannon.getContraption().getContraption()).getWeightForStress();
            return cannonStress * 2;
        } else {
            return 0f;
        }
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

    @Override
    public void onLoad() {
        if(cannon == null && level != null && !level.isClientSide) {
            cannon = findCannon(level);
        }
        read(getPersistentData(), false);
        super.onLoad();
    }

    public float getTargetPitch() {
        return targetPitch;
    }

    public float getTargetYaw() {
        return targetYaw;
    }

    public float getCannonPitch() {
        if(cannon != null && cannon.getCannonMount() != null) {
            return cannon.getContraption().pitch;
        }
        return 0f;
    }

    public float getCannonYaw() {
        if (cannon != null) {
            if (cannon.getCannonMount() != null) {
                return cannon.getContraption().yaw;
            }
            Direction facing = cannon.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            return (float) Utils.yawFromFacing(facing);
        }
        return 0f;
    }

    @Override
    public void removeNetwork() {
        if(network != null) {
            network.removeCannon(this);
        }
    }


}
