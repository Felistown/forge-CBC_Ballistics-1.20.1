package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.entity.model.RadioModel;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.artilleryCoordinator.OpenCoordinatorC2SPacket;
import net.felis.cbc_ballistics.networking.packet.radio.SendRadioDataC2SPacket;
import net.felis.cbc_ballistics.screen.Artillery_CoordinatorInterface;
import net.felis.cbc_ballistics.util.KeyBinding;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RadioItem extends ArmorItem {

    private byte cooldown;
    
    public RadioItem(Properties pProperties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, pProperties);
        cooldown = 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if(pContext.getHand() == InteractionHand.MAIN_HAND && !level.isClientSide) {
            CompoundTag itemTag = pContext.getItemInHand().getOrCreateTag();
            if (level.getBlockEntity(pContext.getClickedPos()) instanceof ArtilleryCoordinatorBlockEntity network) {
                CompoundTag tag = new CompoundTag();
                tag.putIntArray("pos", Utils.blockPosToArray(network.getBlockPos()));
                tag.putString("network_id", network.getNetwork_id());
                itemTag.put("network", tag);
            } else {
                itemTag.remove("network");
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
        if(slotIndex == 38) {
            cooldown = (byte) Math.max(0, cooldown - 1);
            if (cooldown <= 0) {
                cooldown = 5;
                CompoundTag tags = stack.getOrCreateTag();
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RangefinderItem && tags.contains("network_data")) {
                    new Artillery_CoordinatorInterface(tags.getCompound("network_data")).renderSolutions(level);
                }
            }
        }
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                HumanoidModel armourModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
                        Map.of("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "body", new RadioModel(Minecraft.getInstance().getEntityModels().bakeLayer(RadioModel.LAYER_LOCATION)).BODY,
                                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                return armourModel;
            }
        });
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "cbc_ballistics:textures/item/radio.png";
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.cbc_ballistics.radio_item.key_bind1").append(KeyBinding.OPEN_RADIO_KEY.getKey().getDisplayName()).append(Component.translatable("item.cbc_ballistics.radio_item.key_bind2")));
        CompoundTag tags = pStack.getOrCreateTag();
        if(tags.contains("network")) {
            CompoundTag net = tags.getCompound("network");
            pTooltipComponents.add(Component.translatable("unit.cbc_ballistics.artillery_network.network").append(net.getString("network_id")).append(Component.translatable("unit.cbc_ballistics.artillery_network.at")).append(Utils.formatPos(net.getIntArray("pos"))));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
