package net.felis.cbc_ballistics.block;


import com.simibubi.create.foundation.data.ModelGen;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.block.custom.ArtilleryCoordinatorBlock;
import net.felis.cbc_ballistics.block.custom.CalculatorBlock;
import net.felis.cbc_ballistics.block.custom.CannonControllerBlock;
import net.felis.cbc_ballistics.item.ModItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;

import java.util.function.Supplier;

public class ModBlocks {
    public static final CreateRegistrate REGISTRATE = CBC_Ballistics.REGISTRATE;
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CBC_Ballistics.MODID);

    public static final RegistryObject<Block> BALLISTIC_CALCULATOR = registerBlock("ballistic_calculator", () -> new CalculatorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.10f, 3.5F).sound(SoundType.METAL)));

    public static final RegistryEntry<CannonControllerBlock> CANNON_CONTROL = REGISTRATE
            .block("cannon_control", CannonControllerBlock::new)
            .properties(p -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.10f, 3.5F).sound(SoundType.METAL))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .item().transform(ModelGen.customItemModel())
            .addLayer(() -> {return RenderType::cutoutMipped;})
            .register();

    public static final RegistryObject<Block> ARTILLERY_COORDINATOR = registerBlock("artillery_coordinator", () -> new ArtilleryCoordinatorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.10f, 3.5F).sound(SoundType.METAL)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
