package net.felis.cbc_ballistics.item.custom;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.*;
import net.felis.cbc_ballistics.config.CBC_BallisticsCommonConfigs;
import net.felis.cbc_ballistics.entity.custom.DetectingProjectile;
import net.felis.cbc_ballistics.util.IHaveData;
import net.felis.cbc_ballistics.util.ParticleHelper;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.targets.CommonLaunchHandler;
import net.minecraftforge.fml.loading.targets.FMLClientDevLaunchHandler;
import net.minecraftforge.network.PacketDistributor;
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


    public void rangeFind(ItemStack item, Player player) {
        item.setTag(new CompoundTag());
        Level level = player.level();
        Vec3 playerPos = new Vec3(player.getX(), player.getEyeY() - Float.MIN_VALUE, player.getZ());
        HitResult result = new DetectingProjectile.Detect(level, playerPos)
                .range(CBC_BallisticsCommonConfigs.RANGEFINDER_MAX_RANGE.get())
                .simulate(player.getXRot(), player.getYRot(), 10f)
                .getResults();
        if (result != null && result.getType() != HitResult.Type.MISS) {
            playerPos = new Vec3(player.getX(), player.getEyeY() - 0.5f, player.getZ());
            Vec3 pos = new Vec3(0,0,0);
            Vec3 hitPos = new Vec3(0,0,0);
            if(result instanceof BlockHitResult bh) {
                BlockPos blockPos = bh.getBlockPos();
                pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                hitPos = bh.getBlockPos().getCenter();
            } else if(result instanceof EntityHitResult eh){
                pos = eh.getEntity().getBoundingBox().getCenter();
                hitPos = pos;
            }
            ParticleHelper.line(level, playerPos, hitPos, ParticleHelper.Colour.RED, 0.5f);
            String target = Utils.formatPos(pos);
            item.getTag().putString("results", target);
            ItemStack stack = player.getInventory().getArmor(2);
            if(stack.getItem() instanceof RadioItem radio) {
                radio.sendTarget(stack, target);
            }
        } else {
            item.getTag().putString("results", "Too far");
        }
    }
}
