package net.felis.cbc_ballistics.util.artilleryNetwork;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Director extends NetworkComponent {

    public void setTarget(int[] target);

    public void setId(int id);

    public int getId();

    public ArtilleryCoordinatorBlockEntity getNetwork();

    public void setNetwork(ArtilleryCoordinatorBlockEntity network);

    public BlockEntity getBlockEntity();

    public void target();

    public void mode(int mode);

    public void removeNetwork();
}
