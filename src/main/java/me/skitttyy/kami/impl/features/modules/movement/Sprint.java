package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.effect.StatusEffects;

public class Sprint extends Module {
    public static Sprint INSTANCE;

    public Sprint()
    {
        super("Sprint", Category.Movement);
        INSTANCE = this;
    }

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Vanilla")
            .withModes("Vanilla", "Rage")
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .withPageParent(mode)
            .withPage("Rage")
            .register(this);

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (mode.getValue().equals("Rage") && rotate.getValue() && mc.currentScreen == null)
            RotationUtils.setRotation(new float[]{PlayerUtils.getMoveYaw(mc.player.getYaw()), mc.player.getPitch()}, 2);

    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (PlayerUtils.isMoving()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F)
        {
            switch (mode.getValue())
            {
                case "Vanilla" ->
                {
                    if (mc.player.input.hasForwardMovement()
                            && (!mc.player.horizontalCollision
                            || mc.player.collidedSoftly))
                    {
                        mc.player.setSprinting(true);
                    }
                }
                case "Rage" ->
                {
                    if (rotate.getValue())
                        doVanilla();
                    else
                        mc.player.setSprinting(true);
                }
            }
        }
    }

    public void doVanilla()
    {
        if (mc.player.input.hasForwardMovement()
                && (!mc.player.horizontalCollision
                || mc.player.collidedSoftly))
        {
            mc.player.setSprinting(true);
        }
    }

    @SubscribeEvent
    public void onTick(LivingEvent.SetSprinting event)
    {
        if (PlayerUtils.isMoving()
                && !rotate.getValue()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F
                && mode.getValue().equals("Rage"))
        {
            event.setCancelled(true);
        }
    }


    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "Sprint: go 22.22 to the left with this module";
    }
}