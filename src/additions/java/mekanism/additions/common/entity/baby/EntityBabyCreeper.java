package mekanism.additions.common.entity.baby;

import javax.annotation.Nonnull;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityBabyCreeper extends CreeperEntity {

    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.createKey(EntityBabyCreeper.class, DataSerializers.BOOLEAN);

    public EntityBabyCreeper(EntityType<EntityBabyCreeper> type, World world) {
        super(type, world);
        setChild(true);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.getDataManager().register(IS_CHILD, false);
    }

    @Override
    public boolean isChild() {
        return getDataManager().get(IS_CHILD);
    }

    public void setChild(boolean child) {
        getDataManager().set(IS_CHILD, child);
        if (world != null && !world.isRemote) {
            IAttributeInstance attributeInstance = getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            attributeInstance.removeModifier(MekanismAdditions.babySpeedBoostModifier);
            if (child) {
                attributeInstance.applyModifier(MekanismAdditions.babySpeedBoostModifier);
            }
        }
    }

    @Override
    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        if (IS_CHILD.equals(key)) {
            recalculateSize();
        }
        super.notifyDataManagerChange(key);
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        if (isChild()) {
            experienceValue = (int) (experienceValue * 2.5F);
        }
        return super.getExperiencePoints(player);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntitySize size) {
        return isChild() ? 0.77F : super.getStandingEyeHeight(pose, size);
    }

    /**
     * Modify vanilla's explode method to half the explosion strength of baby creepers, and charged baby creepers
     */
    @Override
    protected void explode() {
        if (!world.isRemote) {
            Explosion.Mode mode = ForgeEventFactory.getMobGriefingEvent(world, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            float f = isCharged() ? 1 : 0.5F;
            dead = true;
            world.createExplosion(this, getPosX(), getPosY(), getPosZ(), (float) explosionRadius * f, mode);
            remove();
            spawnLingeringCloud();
        }
    }
}