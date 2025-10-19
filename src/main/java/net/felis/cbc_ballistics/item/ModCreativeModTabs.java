package net.felis.cbc_ballistics.item;

import net.felis.cbc_ballistics.CBC_Ballistics;
import net.felis.cbc_ballistics.block.ModBlocks;
import net.felis.cbc_ballistics.item.custom.RadioItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CBC_Ballistics.MODID);

    public static final RegistryObject<CreativeModeTab> BALLISTICS_TAB = CREATIVE_MODE_TABS.register("ballistics", () -> CreativeModeTab.builder().icon(() ->  new ItemStack(ModItems.RANGEFINDER.get()))
            .title(Component.translatable("creativetab.ballistics"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModItems.RANGEFINDER.get());
                output.accept(ModItems.RADIO.get());
                output.accept(ModItems.MANAGER.get());
                output.accept(ModBlocks.ARTILLERY_COORDINATOR.get());
                output.accept(ModBlocks.BALLISTIC_CALCULATOR.get());
                output.accept(ModBlocks.CANNON_CONTROL.get());
                output.accept(ModItems.WAND.get());
            }).build());

    public  static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}



