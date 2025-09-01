package net.felis.cbc_ballistics.util.artilleryNetwork;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Director {

    public void setTarget(int[] target);

    public void setId(int id);

    public int getId();

    public ArtilleryCoordinatorBlockEntity getNetwork();

    public void setNetwork(ArtilleryCoordinatorBlockEntity network);

    public BlockEntity getBlockEntity();


}
