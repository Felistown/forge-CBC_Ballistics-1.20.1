package net.felis.cbc_ballistics.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;


public class CannonControllerBlockEntity extends KineticBlockEntity implements  IHaveGoggleInformation, Layer {

    private float targetPitch;
    private float targetYaw;
    private float newPitch;
    private float newYaw;
    private CannonMountBlockEntity cannon;
    private boolean active;
    private boolean setting;
    private boolean fire;
    private Director director;
    private ArtilleryCoordinatorBlockEntity network;


    public CannonControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        newPitch = 0f;
        newYaw = 0f;
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
        newPitch = 0f;
        newYaw = 0f;
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

                    if (mount.pitch != -targetPitch) {
                        changed = true;
                        float pDiff = Math.abs(targetPitch + mount.pitch);
                        if (pDiff > speed) {
                            float movement;
                            if (-mount.pitch > targetPitch) {
                                movement = -speed;
                            } else {
                                movement = speed;
                            }
                            newPitch = -mount.pitch + movement;
                        } else {
                            newPitch = targetPitch;
                        }
                    }

                    if (mount.yaw != targetYaw) {
                        changed = true;
                        float yaw = (mount.yaw + 360) % 360;
                        float tYaw = (targetYaw + 360) % 360;
                        float yDiff = Math.abs(tYaw - yaw);
                        if (yDiff > speed) {
                            float movement;
                            if (tYaw < yaw) {
                                movement = -speed;
                            } else {
                                movement = speed;
                            }
                            newYaw = yaw + movement;
                        } else {
                            newYaw = targetYaw;
                        }
                    }
                    if (changed) {
                        update();
                    } else {
                        setting = false;
                        fire();
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

    private void update() {
        if(!overStressed) {
            cannon.setPitch(newPitch);
            cannon.setYaw(newYaw);
            cannon.notifyUpdate();
        }
    }

    private void updateNetwork(boolean active) {
        if(active == this.active) {
            if (getOrCreateNetwork() != null) {
                stress += getAddedStress();
                if(level != null && level.isClientSide) {
                    System.out.println("from upnet");
                    System.out.println(capacity);
                }
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
        newPitch = 0f;
        newYaw = 0f;
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
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putBoolean("setting", setting);
        compound.putFloat("yaw", targetYaw);
        compound.putFloat("pitch", targetPitch);
        compound.putBoolean("active", active);
        CompoundTag tag = getPersistentData();
        tag.putBoolean("setting", setting);
        tag.putFloat("yaw", targetYaw);
        tag.putFloat("pitch", targetPitch);
        tag.putBoolean("active", active);
        if(director != null) {
            BlockPos senderPos =  director.getBlockEntity().getBlockPos();
            int[] senderCords = {senderPos.getX(), senderPos.getY(), senderPos.getZ()};
            compound.putIntArray("senderPos", senderCords);
            tag.putIntArray("senderPos", senderCords);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        write(tag, false);
        return super.getUpdateTag();
    }


    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
            setting = compound.getBoolean("setting");
            targetYaw = compound.getFloat("yaw");
            targetPitch =  compound.getFloat("pitch");
            active = compound.getBoolean("active");
            int[] blockCords = compound.getIntArray("senderPos");
            if(level != null) {
                cannon = findCannon(level);
                if (blockCords.length >= 3) {
                    BlockPos pos = new BlockPos(blockCords[0], blockCords[1], blockCords[2]);
                    BlockEntity sender = level.getBlockEntity(pos);
                    if (sender instanceof Director) {
                        this.director = (Director) sender;
                    }
                }
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
}
