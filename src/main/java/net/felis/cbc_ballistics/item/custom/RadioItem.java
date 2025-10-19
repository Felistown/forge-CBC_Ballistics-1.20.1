package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.OpenCoordinatorC2SPacket;
import net.felis.cbc_ballistics.networking.packet.radio.SendRadioDataC2SPacket;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RadioItem extends ArmorItem {

    private byte cooldown;
    
    public RadioItem(Properties pProperties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, pProperties);
        cooldown = 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if(pContext.getHand() == InteractionHand.MAIN_HAND && !level.isClientSide) {
            if (level.getBlockEntity(pContext.getClickedPos()) instanceof ArtilleryCoordinatorBlockEntity network) {
                CompoundTag tag = new CompoundTag();
                tag.putIntArray("pos", Utils.blockPosToArray(network.getBlockPos()));
                tag.putString("network_id", network.getNetwork_id());
                pContext.getItemInHand().getOrCreateTag().put("network", tag);
            }
        }
        return super.useOn(pContext);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        CompoundTag tags = pStack.getOrCreateTag();
        return tags.contains("network");
    }
    
    public boolean openRadio(ItemStack stack) {
        if(!isFoil(stack)) {
            return false;
        }
        CompoundTag tag = stack.getTag().getCompound("network");
        BlockPos pos = Utils.arrayToBlockPos(tag.getIntArray("pos"));
        ModMessages.sendToServer(new OpenCoordinatorC2SPacket(pos, tag.getString("network_id")));
        return true;
    }
    
    public void sendTarget(ItemStack stack, String target) {
        if(!isFoil(stack)) {
            return;
        }
        CompoundTag tag = stack.getTag().getCompound("network");
        BlockPos pos = Utils.arrayToBlockPos(tag.getIntArray("pos"));
        ModMessages.sendToServer(new SendRadioDataC2SPacket(target, tag.getString("network_id"), pos));
    }
    
    public void receiveData(ItemStack stack, CompoundTag tag) {
        stack.getOrCreateTag().put("network_data", tag);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        cooldown = (byte)Math.max(0, cooldown - 1);
        if(cooldown <= 0) {
            cooldown = 5;
            CompoundTag tags = stack.getOrCreateTag();
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RangefinderItem && tags.contains("network_data")) {
                new Artillery_CoordinatorInterface(tags.getCompound("network_data")).renderSolutions(level);
            }
        }
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }
}
