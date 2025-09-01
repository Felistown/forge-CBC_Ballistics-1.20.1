package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.block.entity.CalculatorBlockEntity;
import net.felis.cbc_ballistics.block.entity.CannonControllerBlockEntity;
import net.felis.cbc_ballistics.util.IHaveData;
import net.felis.cbc_ballistics.util.calculator.Projectile;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

import java.util.List;


public class BallisticsWandItem extends Item implements IHaveData {

    private boolean isCheck;
    private boolean onBlock;

    public BallisticsWandItem(Properties pProperties) {
        super(pProperties);
        isCheck = true;
        onBlock = false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(pUsedHand == InteractionHand.MAIN_HAND && pLevel.isClientSide && !onBlock) {
            isCheck = !isCheck;
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(pContext.getHand() == InteractionHand.MAIN_HAND) {
            onBlock = false;
            BlockEntity block = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
            if (block != null) {
                onBlock = true;
                if (isCheck) {
                    pContext.getItemInHand().setTag(block.getPersistentData().copy());
                } else {
                    if (block instanceof CalculatorBlockEntity) {
                        block = Minecraft.getInstance().player.level().getBlockEntity(pContext.getClickedPos());
                        if (((CalculatorBlockEntity) block).getResult() != null) {
                            Projectile results = ((CalculatorBlockEntity) block).getResult();
                            CompoundTag tags = pContext.getItemInHand().getTag();
                            if(tags == null) {
                                pContext.getItemInHand().setTag(new CompoundTag());
                            }
                            tags.putFloat("pitch", (float) results.getPitch());
                            tags.putFloat("yaw", (float) results.getCannon().getYaw());
                        }
                    } else if (block instanceof CannonMountBlockEntity && !pContext.getLevel().isClientSide) {
                        CompoundTag tags = pContext.getItemInHand().getTag();
                        CannonMountBlockEntity block1 = ((CannonMountBlockEntity) block);
                        block1.setYaw(tags.getFloat("yaw"));
                        block1.setPitch(tags.getFloat("pitch"));
                    } else if(block instanceof CannonControllerBlockEntity) {
                        CompoundTag tags = pContext.getItemInHand().getTag();
                        CannonControllerBlockEntity block1 = (CannonControllerBlockEntity) block;
                        block1.setTarget(tags.getFloat("pitch"), tags.getFloat("yaw"));
                    }
                }
            } else {
                pContext.getItemInHand().setTag(new CompoundTag());
            }
        }
        return super.useOn(pContext);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return !isCheck;
    }

    @Override
    public Component getComponent(ItemStack item) {
        if(item.getTag() == null) {
            return Component.empty();
        }
        CompoundTag tags = item.getTag();
        Object[] list = tags.getAllKeys().toArray();
        String data = "";
        for(int i = 0; i < list.length; i ++) {
            if(list[i] == null) {
                continue;
            } else {
                String key = list[i].toString();
                data += key + " = " + ((Tag)tags.get(key)).getAsString() + "|";
            }
        }
        return Component.literal(data);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getTag() == null) {
            return;
        }
        CompoundTag tags = pStack.getTag();
        Object[] list = tags.getAllKeys().toArray();
        for(int i = 0; i < list.length; i ++) {
            if(list[i] == null) {
                continue;
            } else {
                String key = list[i].toString();
                pTooltipComponents.add(Component.literal(key + " = " + ((Tag)tags.get(key)).getAsString()));
            }
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
