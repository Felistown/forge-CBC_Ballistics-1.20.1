package net.felis.cbc_ballistics.block.custom;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.felis.cbc_ballistics.block.entity.CannonControllerBlockEntity;
import net.felis.cbc_ballistics.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class CannonControllerBlock extends DirectionalKineticBlock implements IBE<CannonControllerBlockEntity> {

    public CannonControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        if(!worldIn.isClientSide) {
            BlockEntity block = worldIn.getBlockEntity(pos);
            if(block instanceof CannonControllerBlockEntity) {
                ((CannonControllerBlockEntity) block).onPlace(worldIn);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        if(!pLevel.isClientSide) {
            BlockEntity block = pLevel.getBlockEntity(pPos);
            if(block instanceof CannonControllerBlockEntity) {
                ((CannonControllerBlockEntity) block).neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
            }
        }
    }

    @Override
    public Class<CannonControllerBlockEntity> getBlockEntityClass() {
        return CannonControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CannonControllerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CANNON_CONTROLLER_BLOCK_ENTITY.get();
    }


    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown()) || preferred == null)
            return super.getStateForPlacement(context);
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        ((CannonControllerBlockEntity)pLevel.getBlockEntity(pPos)).onRemove();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.CASING_14PX.get(pState.getValue(FACING));

    }
}