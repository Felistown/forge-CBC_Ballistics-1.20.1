package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.config.CBC_BallisticsCommonConfigs;
import net.felis.cbc_ballistics.util.IHaveData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class RangefinderItem extends Item implements IHaveData {

    public RangefinderItem(Properties pProperties) {
        super(pProperties);

    }

    public int getUseDuration(ItemStack pStack) {
        return 1200;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPYGLASS;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        this.stopUsing(pLivingEntity);
        return pStack;
    }

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        this.stopUsing(pLivingEntity);
    }

    private void stopUsing(LivingEntity pUser) {
        pUser.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getTag() != null) {
            pTooltipComponents.add(Component.translatable("tooltip.cbc_ballistics.rangefinder.results").append(pStack.getTag().getString("results")));
        }
        if(Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.cbc_ballistics.rangefinder.shift_down.one").append(Component.literal(CBC_BallisticsCommonConfigs.RANGEFINDER_MAX_RANGE.get().toString()).append(Component.translatable("tooltip.cbc_ballistics.rangefinder.shift_down.two"))));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.cbc_ballistics.rangefinder"));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public Component getComponent(ItemStack item) {
        if(item.getTag() == null) {
            return Component.empty();
        }
        return Component.literal(item.getTag().getString("results"));
    }
}
