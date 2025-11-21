package me.skitttyy.kami.impl.features.modules.combat;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.EntityTargeter;
import me.skitttyy.kami.api.utils.targeting.Sorting;
import me.skitttyy.kami.api.utils.world.EntityUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class BowAim extends Module
{
    Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(true)
            .register(this);
    Value<Boolean> monsters = new ValueBuilder<Boolean>()
            .withDescriptor("Monsters")
            .withValue(true)
            .register(this);
    Value<Boolean> animals = new ValueBuilder<Boolean>()
            .withDescriptor("Animals")
            .withValue(false)
            .register(this);
    Value<Boolean> neutrals = new ValueBuilder<Boolean>()
            .withDescriptor("Neutrals")
            .withValue(false)
            .register(this);
    Value<Boolean> invisibles = new ValueBuilder<Boolean>()
            .withDescriptor("Invisibles")
            .withValue(true)
            .register(this);


    public BowAim()
    {
        super("BowAim", Category.Combat);
    }


    private Entity aimTarget;


    @SubscribeEvent
    public void onTickPre(TickEvent.PlayerTickEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;


        aimTarget = null;
        if (mc.player.getMainHandStack().getItem() instanceof BowItem
                && mc.player.getItemUseTime() >= 3)
        {
            double minDist = Double.MAX_VALUE;
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || entity == mc.player || !entity.isAlive()
                        || !isValidAimTarget(entity)
                        || FriendManager.INSTANCE.isFriend(entity))
                {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist < minDist)
                {
                    minDist = dist;
                    aimTarget = entity;
                }
            }
            if (aimTarget instanceof LivingEntity target)
            {
                float[] rotations = getBowRotationsTo(target);
                RotationUtils.setRotation(rotations[0], rotations[1]);
            }
        }

    }

    private float[] getBowRotationsTo(Entity entity)
    {
        float duration = (float) (mc.player.getActiveItem().getMaxUseTime(mc.player) - mc.player.getItemUseTime()) / 20.0f;
        duration = (duration * duration + duration * 2.0f) / 3.0f;
        if (duration >= 1.0f)
        {
            duration = 1.0f;
        }
        double duration1 = duration * 3.0f;
        double coeff = 0.05000000074505806;
        float pitch = (float) (-Math.toDegrees(calculateArc(entity, duration1, coeff)));
        double ix = entity.getX() - entity.prevX;
        double iz = entity.getZ() - entity.prevZ;
        double d = mc.player.distanceTo(entity);
        d -= d % 2.0;
        ix = d / 2.0 * ix * (mc.player.isSprinting() ? 1.3 : 1.1);
        iz = d / 2.0 * iz * (mc.player.isSprinting() ? 1.3 : 1.1);
        float yaw = (float) Math.toDegrees(Math.atan2(entity.getZ() + iz - mc.player.getZ(), entity.getX() + ix - mc.player.getX())) - 90.0f;
        return new float[]{yaw, pitch};
    }

    private float calculateArc(Entity target, double duration, double coeff)
    {
        double yArc = target.getY() + (double) (target.getStandingEyeHeight() / 2.0f) - (mc.player.getY() + (double) mc.player.getStandingEyeHeight());
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double root, double yArc)
    {
        double dirCoeff = coeff * (root * root);
        yArc = 2.0 * yArc * (duration * duration);
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);
        duration = duration * duration - yArc;
        yArc = Math.atan2(duration * duration + yArc, coeff * root);
        duration = Math.atan2(duration, coeff * root);
        return (float) Math.min(yArc, duration);
    }

    private boolean isValidAimTarget(Entity entity)
    {
        if (entity.isInvisible() && !invisibles.getValue())
        {
            return false;
        }
        return entity instanceof PlayerEntity && players.getValue()
                || EntityUtils.isMonster(entity) && monsters.getValue()
                || EntityUtils.isNeutral(entity) && neutrals.getValue()
                || EntityUtils.isPassive(entity) && animals.getValue();
    }

    @Override
    public String getDescription()
    {
        return "BowAim: Bow aimbot";
    }
}
