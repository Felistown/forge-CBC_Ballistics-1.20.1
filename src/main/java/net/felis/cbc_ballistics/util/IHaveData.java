package net.felis.cbc_ballistics.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IHaveData {

    public Component getComponent(ItemStack item);
}
