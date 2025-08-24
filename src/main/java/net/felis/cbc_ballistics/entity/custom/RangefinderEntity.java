package net.felis.cbc_ballistics.entity.custom;


import net.felis.cbc_ballistics.config.CBC_BallisticsCommonConfigs;
import net.felis.cbc_ballistics.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.ForgeEventFactory;

public class RangefinderEntity extends Projectile {

    private CompoundTag tag;

    public RangefinderEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public RangefinderEntity(Level level, CompoundTag tag) {
        super(ModEntities.RANGEFINDERENTITY.get(), level);
        this.tag = tag;
    }


    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        BlockState blockstate = this.level().getBlockState(pResult.getBlockPos());
        blockstate.onProjectileHit(this.level(), blockstate, pResult, this);
        BlockPos pos = pResult.getBlockPos();
        if(tag != null) {
            tag.putString("results", "X = " + pos.getX() + ", Y = " + pos.getY() + ", Z = " + pos.getZ());
        }
        super.onHitBlock(pResult);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(tag != null) {
            if (pResult.getEntity().getType() == EntityType.CAT) {
                tag.putString("results", "Meow");
            } else {
                Vec3 pos = pResult.getLocation();
                int x = (int)Math.round(pos.x);
                int y = (int)Math.round(pos.y);
                int z = (int)Math.round(pos.z);
                tag.putString("results", "X = " + x + ", Y = " + y + ", Z = " + z);
            }
        }
        super.onHitEntity(pResult);
        this.discard();
    }

    @Override
    protected void defineSynchedData() {

    }

    public void tick() {
        super.tick();
        boolean hitBlock = false;
        int range = (int)Math.round(CBC_BallisticsCommonConfigs.RANGEFINDER_MAX_RANGE.get() / 10f);
        for(int i = 0; i < range && !hitBlock; i ++) {
            hitBlock = subtick();
        }
        if(!hitBlock) {
            if(tag != null) {
                tag.putString("results", "Too far");
                this.discard();
            }
        }
    }

    public boolean subtick() {
        this.checkInsideBlocks();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            if(!(hitresult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitresult).getEntity().equals(getOwner()))) {
                this.onHit(hitresult);
                return true;
            }
        }
        Vec3 vec3 = this.getDeltaMovement();
        double d5 = vec3.x;
        double d6 = vec3.y;
        double d1 = vec3.z;
        double d7 = this.getX() + d5;
        double d2 = this.getY() + d6;
        double d3 = this.getZ() + d1;
        float f = 0.99F;
        this.setDeltaMovement(vec3.scale((double)f));
        this.setPos(d7, d2, d3);
        this.checkInsideBlocks();
        return false;
    }
}
