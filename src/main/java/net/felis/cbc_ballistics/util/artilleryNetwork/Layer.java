package net.felis.cbc_ballistics.util.artilleryNetwork;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Layer {

    public void setDirector(Director director);

    public Director getDirector();

    public ArtilleryCoordinatorBlockEntity getNetwork();

    public void setNetwork(ArtilleryCoordinatorBlockEntity network);

    public BlockEntity getBlockEntity();

    public void setTarget(float pitch, float yaw);

    public boolean isSet();

    public void fire();
}
