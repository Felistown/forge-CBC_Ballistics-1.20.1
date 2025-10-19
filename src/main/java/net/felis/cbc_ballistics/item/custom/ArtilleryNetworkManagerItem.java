package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.networking.ModMessages;
import net.felis.cbc_ballistics.networking.packet.SyncManagerItemS2CPacket;
import net.felis.cbc_ballistics.util.IHaveData;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.felis.cbc_ballistics.util.artilleryNetwork.NetworkComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import oshi.jna.platform.linux.LinuxLibc;

import java.util.List;

public class ArtilleryNetworkManagerItem extends Item implements IHaveData {

    private ArtilleryCoordinatorBlockEntity network;
    private Director selected;
    private int cooldown;

    public ArtilleryNetworkManagerItem(Properties pProperties) {
        super(pProperties);
        cooldown = 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if(pContext.getHand() == InteractionHand.MAIN_HAND && !level.isClientSide) {
            Player player = pContext.getPlayer();
            BlockEntity be = level.getBlockEntity(pContext.getClickedPos());
            if(be instanceof NetworkComponent c) {
                if (network == null) {
                    network = c.getNetwork();
                    if(c instanceof Director d) {
                        selected = d;
                    }
                } else {
                    if(c.getNetwork() == network) {
                        if (c instanceof Director d) {
                            if (d == selected) {
                                disconnect(d);
                            } else {
                                selected = d;
                            }
                        } else {
                            disconnect(c);
                        }
                    } else if(c instanceof ArtilleryCoordinatorBlockEntity n) {
                        ArtilleryCoordinatorBlockEntity s = n.getSuperior();
                        if((s != null && s == network) || n.getSuboridinates().contains(network)) {
                            disconnect(c);
                        } else {
                            connect(c);
                        }
                    } else {
                        connect(c);
                    }
                }
                if(network != null) {
                    network.safeSyncToClient(pContext.getPlayer());
                }
                syncToClient(player);
            } else {
                network = null;
                selected = null;
                syncToClient(player);
            }
        }
        return super.useOn(pContext);
    }


    public void setNetwork(ArtilleryCoordinatorBlockEntity net) {
        network = net;
    }

    public void setSelected(Director dir) {
        selected = dir;
    }

    private void connect(NetworkComponent c) {
        if(c instanceof ArtilleryCoordinatorBlockEntity n && !n.getSuboridinates().contains(network) && Utils.distFrom(n, network) <= 1000000000) {
            network.superiorOf(n);
        } else if(c.getNetwork() == null) {
            if(c instanceof Director d  && Utils.distFrom(d.getBlockEntity(), network) <= 50) {
                network.addDirector(d);
                selected = d;
            } else if (selected != null && c instanceof Layer l && Utils.distFrom(l.getBlockEntity(), selected.getBlockEntity()) <= 25) {
                network.addCannon(l, selected);
            }
        }
    }

    private void disconnect(NetworkComponent c) {
        if(c instanceof ArtilleryCoordinatorBlockEntity n) {
            network.removeNetwork(n);
        } else if(c.getNetwork() == network) {
            if (c instanceof Director d && selected == d) {
                network.removeDirector(d);
            } else if (c instanceof Layer l) {
                network.removeCannon(l);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return network != null;
    }

    @Override
    public Component getComponent(ItemStack item) {
        if(network == null) {
            return Component.translatable("item.cbc_ballistics.network_manager.no_network");
        } else if(selected != null) {
            return Component.translatable("item.cbc_ballistics.network_manager.selected").append("" + selected.getId());
        } else {
            return Component.translatable("item.cbc_ballistics.network_manager.no_selected");
        }
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        cooldown = Math.max(0, cooldown - 1);
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == this &&cooldown <= 0 && level != null && level.isClientSide && network != null) {
            network.line(level, true);
            cooldown = 5;
        }
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(network != null) {
            network.appendForTooltip(pTooltipComponents);
        } else {
            pTooltipComponents.add(Component.translatable("item.cbc_ballistics.network_manager.no_network"));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    public void syncToClient(Player player) {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            ModMessages.sendToPlayer(new SyncManagerItemS2CPacket(network, selected), (ServerPlayer) player);
        });
    }
}
