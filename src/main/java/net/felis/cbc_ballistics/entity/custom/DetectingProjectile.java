package net.felis.cbc_ballistics.entity.custom;


import net.felis.cbc_ballistics.config.CBC_BallisticsCommonConfigs;
import net.felis.cbc_ballistics.entity.ModEntities;
import net.felis.cbc_ballistics.util.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Vector3f;

public class DetectingProjectile extends Projectile {

    private HitResult results;
    private int range;
    private float drag;
    private float grav;
    private boolean allowHitEntity;
    private double distanceTravelled;
    private float angle_of_attack;

    public DetectingProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HitResult getResults() {
        return results;
    }

    public double getDistTravelled() {
        return distanceTravelled;
    }

    private DetectingProjectile(Level level, Vec3 pos, float drag, float grav, boolean allowHitEntity, int range) {
        super(ModEntities.DETECTING_PROJECTILE.get(), level);
        this.grav = grav;
        this.drag = drag;
        this.range = range;
        distanceTravelled = 0;
        this.allowHitEntity = allowHitEntity;
        setPos(pos);
    }

    @Override
    protected void onHit(HitResult pResult) {
        Vec3 loc = pResult.getLocation();
        Vec3 pos = position();
        distanceTravelled += Utils.distFrom(loc, pos);
        angle_of_attack = getXRot() % 90;
        super.onHit(pResult);
    }

    public float getAngle_of_attack() {
        return angle_of_attack;
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        results = pResult;
        super.onHitBlock(pResult);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        results = pResult;
        super.onHitEntity(pResult);
        this.discard();
    }

    @Override
    protected void defineSynchedData() {
    }

    private HitResult simulate(float xRot, float yRot, float pVelocity) {
        Vector3f vel = Utils.vecToVel(xRot, yRot, 1);
        super.shoot(vel.x, vel.y, vel.z, pVelocity, 0);
        boolean hitBlock = false;
        for(int i = 0; i < range && !hitBlock; i ++) {
            hitBlock = subtick();
        }
        if(!hitBlock) {
            this.discard();
        }
        return results;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        if(allowHitEntity) {
            return super.canHitEntity(pTarget);
        }
        return false;
    }

    private boolean subtick() {
        this.checkInsideBlocks();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            if(!(hitresult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitresult).getEntity().equals(getOwner()))) {
                this.onHit(hitresult);
                return true;
            }
        }
        Vec3 vel = this.getDeltaMovement();
        distanceTravelled += vel.length();
        double xVel = vel.x;
        double yVel = vel.y;
        double zVel = vel.z;
        double xPos = this.getX() + xVel;
        double yPos = this.getY() + yVel;
        double zPos = this.getZ() + zVel;
        this.setDeltaMovement(xVel * drag, yVel * drag - grav, zVel * drag);
        this.setPos(xPos, yPos, zPos);
        this.checkInsideBlocks();
        return false;
    }

    public static class Detect {

        private Vec3 pos;
        private float drag = 1;
        private float grav = 0;
        private boolean hitEntity = true;
        private Level level;
        private int range = 64;

        public Detect(Level level, Vec3 pos) {
            this.level = level;
            this.pos = pos;
        }

        public Detect(Level level, double x, double y, double z) {
            this.level = level;
            this.pos = new Vec3(x, y, z);
        }

        public Detect drag(float drag) {
            this.drag = drag;
            return this;
        }

        public Detect grav(float grav) {
            this.grav = grav;
            return this;
        }

        public Detect range(int range) {
            this.range = range;
            return this;
        }

        public Detect allowHitEntity(boolean hitEntity) {
            this.hitEntity = hitEntity;
            return this;
        }

        public DetectingProjectile simulate(float xRot, float yRot, float pVelocity) {
            DetectingProjectile projectile = new DetectingProjectile(level, pos, drag, grav, hitEntity, range);
            projectile.simulate(xRot, yRot, pVelocity);
            return projectile;
        }
    }
}
