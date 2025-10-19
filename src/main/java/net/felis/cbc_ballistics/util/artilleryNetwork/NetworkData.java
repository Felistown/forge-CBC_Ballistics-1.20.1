package net.felis.cbc_ballistics.util.artilleryNetwork;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;

public class NetworkData {

    private static NetworkData clientData;
    private CompoundTag tags;

    public NetworkData(ArtilleryCoordinatorBlockEntity be) {
        tags = new CompoundTag();
        tags.putString("net_id", be.getNetwork_id());
        tags.putIntArray("target", be.getTargetPos());
        ArrayList<Director> directors = be.getDirectors();
        tags.putInt("num_dir", directors.size());
        for(int i = 0; i < directors.size(); i ++) {
            tags.putIntArray("proj" + i, new int[] {

            });
        }
    }

    public void updateFrom(CompoundTag tags) {
        for(String key: tags.getAllKeys()) {
            if(tags.contains(key)) {
                tags.put(key, tags.get(key));
            }
        }
    }

    public static NetworkData getClientData() {
        if(Dist.CLIENT.isClient() && clientData != null) {
            return clientData;
        }
        return null;
    }

    public static void setClientData(NetworkData data) {
        if(Dist.CLIENT.isClient()) {
            clientData = data;
        }
    }

}
