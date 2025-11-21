package me.skitttyy.kami.impl.features.modules.combat;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.targeting.EntityTargeter;
import me.skitttyy.kami.api.utils.targeting.Sorting;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AimAssist extends Module {

    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(3, 6)
            .withPlaces(1)
            .register(this);
    public Value<Number> fov = new ValueBuilder<Number>()
            .withDescriptor("FOV")
            .withValue(90.0)
            .withRange(1, 360.0D)
            .withPlaces(1)
            .register(this);
    Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(true)
            .register(this);
    Value<Boolean> mobs = new ValueBuilder<Boolean>()
            .withDescriptor("Mobs")
            .withValue(false)
            .register(this);
    Value<Boolean> animals = new ValueBuilder<Boolean>()
            .withDescriptor("Animals")
            .withValue(false)
            .register(this);
    Value<Boolean> vehicles = new ValueBuilder<Boolean>()
            .withDescriptor("Vehicles")
            .withValue(false)
            .register(this);

    public Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(2.5)
            .withRange(0.1, 5.0)
            .withPlaces(2)
            .register(this);
    public Value<Number> force = new ValueBuilder<Number>()
            .withDescriptor("Force")
            .withValue(10)
            .withRange(0.1, 15.0)
            .withPlaces(2)
            .register(this);
    Value<Boolean> breakBlocks = new ValueBuilder<Boolean>()
            .withDescriptor("Break Blocks")
            .withValue(false)
            .register(this);
    Value<Boolean> hold = new ValueBuilder<Boolean>()
            .withDescriptor("Hold")
            .withValue(false)
            .register(this);
    Value<Boolean> onlySword = new ValueBuilder<Boolean>()
            .withDescriptor("Only Sword")
            .withValue(false)
            .register(this);

    public AimAssist()
    {
        super("AimAssist", Category.Combat);
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (breakBlocks.getValue() && breakBlock()) return;


        if (hold.getValue() && !mc.options.attackKey.isPressed()) return;

        if (onlySword.getValue()
                && !(mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof MaceItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem)) {
            return;
        }
        ArrayList<EntityType> toTarget = new ArrayList<>();
        if (players.getValue())
            toTarget.add(EntityType.PLAYER);

        if (mobs.getValue())
            toTarget.add(EntityType.ZOMBIE);

        if (animals.getValue())
            toTarget.add(EntityType.PIG);

        if (vehicles.getValue())
            toTarget.add(EntityType.MINECART);

        Entity target = new EntityTargeter(mc.player, Sorting.FOV, range.getValue().floatValue(), toTarget, fov.getValue().doubleValue()).findTarget(mc.player.getEyePos());

        if (target != null)
        {
            double change = PlayerUtils.fovFromEntity(target);


            if (change > 1.0D || change < -1.0D)
            {

                double complimentSpeed = change * (ThreadLocalRandom.current().nextDouble(force.getValue().doubleValue() - 1.47328, force.getValue().doubleValue() + 2.48293) / 100);
                float val = (float) (-(complimentSpeed + change / (101.0D - (float) ThreadLocalRandom.current().nextDouble(speed.getValue().doubleValue() - 4.723847, speed.getValue().doubleValue()))));
                float[] newRots = {mc.player.getYaw() + val, 0};


                mc.player.setYaw(roundRotation(newRots)[0]);

            }
        }

    }
    public float[] roundRotation(float[] rotation)
    {
        float f = mc.options.getMouseSensitivity().getValue().floatValue() * 0.6f + 0.2f;
        float f1 = f * f * f * 1.2f;
        float yaw = rotation[0] - (rotation[0] % (f1 / 4));
        float pitch = rotation[1] - (rotation[1] % (f1 / 4));
        return new float[]{yaw, pitch};
    }
    public boolean breakBlock()
    {
        final HitResult result = mc.crosshairTarget;
        if (result != null)
        {
            if (result.getType() == HitResult.Type.BLOCK)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        return "AimAssist: aims smoothly at nearby enemies";
    }
}
