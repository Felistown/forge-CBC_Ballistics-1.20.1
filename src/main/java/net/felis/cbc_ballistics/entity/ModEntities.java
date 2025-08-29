package net.felis.cbc_ballistics.entity;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.entity.custom.RangefinderEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>>  ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CBC_Ballistics.MODID);



    public static final RegistryObject<EntityType<RangefinderEntity>> RANGEFINDERENTITY =
            ENTITY_TYPES.register("rangefinderentity", () -> EntityType.Builder.<RangefinderEntity>of(RangefinderEntity ::new, MobCategory.MISC)
                    .sized(Float.MIN_VALUE * 2, Float.MIN_VALUE * 2).build("rangefinderentity"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
