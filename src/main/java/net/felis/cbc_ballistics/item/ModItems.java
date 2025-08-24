package net.felis.cbc_ballistics.item;

import net.felis.cbc_ballistics.CBS_Ballistics;
import net.felis.cbc_ballistics.item.custom.BallisticsWandItem;
import net.felis.cbc_ballistics.item.custom.RangefinderItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CBS_Ballistics.MODID);

    public static final RegistryObject<Item> RANGEFINDER = ITEMS.register("rangefinder", () -> new RangefinderItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WAND = ITEMS.register("ballistics_wand", () -> new BallisticsWandItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
