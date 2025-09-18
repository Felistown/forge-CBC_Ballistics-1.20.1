package net.felis.cbc_ballistics.block;


import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.block.custom.ArtilleryCoordinatorBlock;
import net.felis.cbc_ballistics.block.custom.CalculatorBlock;
import net.felis.cbc_ballistics.block.custom.CannonControllerBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;


public class ModBlocks {
    public static final CreateRegistrate REGISTRATE = CBC_Ballistics.REGISTRATE;

    public static final RegistryEntry<CalculatorBlock> BALLISTIC_CALCULATOR = REGISTRATE
            .block("ballistic_calculator", CalculatorBlock::new)
            .properties(p -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1).requiresCorrectToolForDrops().strength(1.5f))
            .item().transform(ModelGen.customItemModel())
            .transform(TagGen.axeOrPickaxe())
            .item().properties(p -> p.stacksTo(16)).build()
            .register();

    public static final RegistryEntry<CannonControllerBlock> CANNON_CONTROL = REGISTRATE
            .block("cannon_control", CannonControllerBlock::new)
            .properties(p -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1).requiresCorrectToolForDrops().strength(1.5f))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .item().transform(ModelGen.customItemModel())
            .transform(TagGen.axeOrPickaxe())
            .item().properties(p -> p.stacksTo(16)).build()
            .addLayer(() -> {return RenderType::cutoutMipped;})
            .register();

    public static final RegistryEntry<ArtilleryCoordinatorBlock> ARTILLERY_COORDINATOR = REGISTRATE
            .block("artillery_coordinator", ArtilleryCoordinatorBlock::new)
            .properties(p -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1).requiresCorrectToolForDrops().strength(1.5f))
            .item().transform(ModelGen.customItemModel())
            .item().properties(p -> p.stacksTo(16)).build()
            .transform(TagGen.axeOrPickaxe())
            .register();
}
