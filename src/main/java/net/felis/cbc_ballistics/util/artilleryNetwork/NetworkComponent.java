package net.felis.cbc_ballistics.util.artilleryNetwork;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;

public interface NetworkComponent {

    public ArtilleryCoordinatorBlockEntity getNetwork();

    public void removeNetwork();
}
