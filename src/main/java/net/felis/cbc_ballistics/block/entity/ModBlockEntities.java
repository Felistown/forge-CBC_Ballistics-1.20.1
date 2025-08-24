package net.felis.cbc_ballistics.block.entity;

import net.felis.cbc_ballistics.CBS_Ballistics;
import net.felis.cbc_ballistics.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CBS_Ballistics.MODID);

    public static final RegistryObject<BlockEntityType<CalculatorBlockEntity>> CALCULATORBLOCKENTITY =
            BLOCK_ENTITIES.register("calculatorblockentity", () -> BlockEntityType.Builder.of(CalculatorBlockEntity::new, ModBlocks.BALLISTIC_CALCULATOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
