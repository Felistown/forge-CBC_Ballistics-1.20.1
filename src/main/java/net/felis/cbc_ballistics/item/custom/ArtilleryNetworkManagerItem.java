package net.felis.cbc_ballistics.item.custom;

import net.felis.cbc_ballistics.block.entity.ArtilleryCoordinatorBlockEntity;
import net.felis.cbc_ballistics.util.Utils;
import net.felis.cbc_ballistics.util.IHaveData;
import net.felis.cbc_ballistics.util.artilleryNetwork.Director;
import net.felis.cbc_ballistics.util.artilleryNetwork.Layer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtilleryNetworkManagerItem extends Item implements IHaveData {

    private ArtilleryCoordinatorBlockEntity network;
    private Director selected;

    public ArtilleryNetworkManagerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(pContext.getHand() == InteractionHand.MAIN_HAND && !pContext.getLevel().isClientSide) {
            BlockEntity block = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
            if (block != null) {
                if(block instanceof ArtilleryCoordinatorBlockEntity) {
                    ArtilleryCoordinatorBlockEntity be = (ArtilleryCoordinatorBlockEntity) block;
                    if(network == null) {
                        selected = null;
                        network = (ArtilleryCoordinatorBlockEntity) block;
                    } else if(!network.partOfNetwork(be)){
                        if(Utils.distFrom(network, be) < 50) {
                            network.superiorOf(be);
                            network.setChanged();
                            network = be;
                            network.setChanged();
                        } else {
                            System.out.println("too far");
                        }
                    }
                } else if(block instanceof Director) {
                    Director director = (Director) block;
                    if (director.getNetwork() != null) {
                        network = director.getNetwork();
                        selected = director;
                    } else if(network != null) {
                        if(Utils.distFrom(network, block) < 50) {
                            network.addDirector(director);
                            network.setChanged();
                            selected = director;
                        } else {
                            System.out.println("too far");
                        }
                    }
                } else if(block instanceof Layer) {
                    Layer layer = (Layer) block;
                    if (layer.getNetwork() != null) {
                        network = layer.getNetwork();
                    } else if(network != null && selected != null) {
                        if(Utils.distFrom(network, block) < 50) {
                            network.addCannon(layer, selected);
                            network.setChanged();;
                        } else {
                            System.out.println("too far");
                        }
                    }
                }
            } else {
                network = null;
                selected = null;
            }
        }
        return super.useOn(pContext);
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(network != null) {
            network.appendForTooltip(pTooltipComponents, Screen.hasShiftDown());
        } else {
            pTooltipComponents.add(Component.translatable("item.cbc_ballistics.network_manager.no_network"));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

}
