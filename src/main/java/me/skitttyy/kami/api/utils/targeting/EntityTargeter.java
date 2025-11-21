package me.skitttyy.kami.api.utils.targeting;

import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;


public class EntityTargeter implements IMinecraft {
    Entity targetFrom;
    Sorting mode;
    float range;
    ArrayList<EntityType> toTarget;
    double maxFovRange;

    public EntityTargeter(Entity targetFrom, Sorting mode, float range, ArrayList<EntityType> toTarget)
    {
        this.targetFrom = targetFrom;
        this.mode = mode;
        this.range = range;
        this.toTarget = toTarget;
        maxFovRange = Double.MAX_VALUE;
    }

    public EntityTargeter(Entity targetFrom, Sorting mode, float range, ArrayList<EntityType> toTarget, double maxFovRange)
    {
        this.targetFrom = targetFrom;
        this.mode = mode;
        this.range = range;
        this.toTarget = toTarget;
        this.maxFovRange = maxFovRange;
    }



    public Entity findTarget(Vec3d pos)
    {
        Entity target = null;

        double maxDist = range;
        double maxFovChange = maxFovRange;
        for (Entity e : mc.world.getEntities())
        {
            if (e != null)
            {
                if (canTarget(e) && isValid(e))
                {
                    double currentDist = pos.distanceTo(e.getEyePos());
                    if (currentDist <= maxDist)
                    {
                        if (mode.equals(Sorting.DISTANCE))
                        {
                            maxDist = currentDist;
                            target = e;
                        } else if (mode.equals(Sorting.FOV))
                        {
                            double change;
                            if (maxFovChange > (change = Math.abs(PlayerUtils.fovFromEntity(e))))
                            {
                                target = e;
                                maxFovChange = change;
                            }
                        }
                    }
                }
            }
        }
        return target;
    }

    private boolean canTarget(Entity entity)
    {
        if (FriendManager.INSTANCE.isFriend(entity))
        {
            return false;
        }

        if (toTarget.contains(EntityType.ZOMBIE) && entity instanceof HostileEntity)
        {
            return true;
        }
        if (toTarget.contains(EntityType.PIG) && entity instanceof AnimalEntity)
        {
            return true;
        }
        if ((entity instanceof BoatEntity || entity instanceof MinecartEntity) && toTarget.contains(EntityType.BOAT))
        {
            return true;
        }

        return toTarget.contains(entity.getType());
    }

    public boolean isValid(Entity entity)
    {
        if (!(entity instanceof LivingEntity ent))
        {
            return false;
        }
        if (ent.getHealth() <= 0 || ent.isDead())
        {
            return false;
        }
        return entity != targetFrom;
    }
}
