package net.felis.cbc_ballistics.block.entity;

import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
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

    private static final CreateRegistrate REGISTRATE = CBS_Ballistics.REGISTRATE;

    public static final RegistryObject<BlockEntityType<CalculatorBlockEntity>> CALCULATORBLOCKENTITY =
            BLOCK_ENTITIES.register("calculatorblockentity", () -> BlockEntityType.Builder.of(CalculatorBlockEntity::new, ModBlocks.BALLISTIC_CALCULATOR.get()).build(null));

    public static final BlockEntityEntry<CannonControllerBlockEntity> CANNON_CONTROLLER_BLOCK_ENTITY = REGISTRATE
            .blockEntity("cannon_control_block_entity", CannonControllerBlockEntity::new)
            .visual(() -> ShaftVisual::new, true)
            .validBlock(ModBlocks.CANNON_CONTROL)
            .register();

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
