package net.felis.cbc_ballistics.entity;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.entity.custom.DetectingProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>>  ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CBC_Ballistics.MODID);



    public static final RegistryObject<EntityType<DetectingProjectile>> DETECTING_PROJECTILE =
            ENTITY_TYPES.register("detecting_projectile", () -> EntityType.Builder.<DetectingProjectile>of(DetectingProjectile::new, MobCategory.MISC)
                    .sized(Float.MIN_VALUE * 2, Float.MIN_VALUE * 2).build("detecting_projectile"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
