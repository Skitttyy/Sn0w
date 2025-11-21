package me.skitttyy.kami.impl.features.modules.ghost;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IMinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;


public class LegitCrystal extends Module
{


    Value<Number> breakDelay = new ValueBuilder<Number>()
            .withDescriptor("Break Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    Value<Number> placeDelay = new ValueBuilder<Number>()
            .withDescriptor("Place Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    public Value<Boolean> autoDtap = new ValueBuilder<Boolean>()
            .withDescriptor("Lethal Tick")
            .withValue(false)
            .register(this);
    Value<Number> dtapDelay = new ValueBuilder<Number>()
            .withDescriptor("Crystal Delay")
            .withValue(300)
            .withRange(0, 700)
            .withParent(autoDtap)
            .withParentEnabled(true)
            .register(this);

    public LegitCrystal()
    {
        super("LegitCrystal", Category.Ghost);
    }

    Timer breakTimer = new Timer();
    Timer placeTimer = new Timer();

    Timer lethalDelay = new Timer();

    boolean doingAutoDtap = false;
    boolean didAutoDtapAttack = false;
    boolean didAutoDtapCrystal = false;

    private boolean isDeadBodyNearby()
    {
        return mc.world.getPlayers().parallelStream()
                .filter(e -> mc.player != e)
                .filter(e -> e.squaredDistanceTo(mc.player) < 36)
                .anyMatch(PlayerEntity::isDead);
    }

    LivingEntity target = null;


    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.VanillaTick event)
    {
        if (NullUtils.nullCheck()) return;


        breakTimer.setDelay(breakDelay.getValue().longValue());
        placeTimer.setDelay(placeDelay.getValue().longValue());

        lethalDelay.setDelay(dtapDelay.getValue().longValue());


        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (!mainHandStack.isOf(Items.END_CRYSTAL))
            return;


        if (!mc.options.useKey.isPressed()) return;

        if (isDeadBodyNearby())
            return;


        if (mc.crosshairTarget instanceof BlockHitResult hit)
        {
            BlockPos block = hit.getBlockPos();
            if (placeTimer.isPassed() && CrystalUtil.canPlaceCrystalServer(block))
            {
                ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                placeTimer.resetDelay();
                ((IMinecraftClient) mc).setItemUseCooldown(10);

                if (result == ActionResult.SUCCESS)
                    mc.player.swingHand(Hand.MAIN_HAND);

                return;
            }
        } else if (mc.crosshairTarget instanceof EntityHitResult hit)
        {



                if (lethalDelay.isPassed())
                {
                    doingAutoDtap = false;
                    didAutoDtapAttack = false;
                    didAutoDtapCrystal = false;
                    target = null;
                }

            if (breakTimer.isPassed())
            {
                if (hit.getEntity() instanceof EndCrystalEntity crystal)
                {
                    if (doingAutoDtap && didAutoDtapAttack && didAutoDtapCrystal && !lethalDelay.isPassed())
                    {
                        return;
                    }
                    mc.interactionManager.attackEntity(mc.player, crystal);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    breakTimer.resetDelay();

                    if (doingAutoDtap && didAutoDtapAttack && !didAutoDtapCrystal)
                    {
                        didAutoDtapCrystal = true;
                    }

                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(LivingEvent.Attack event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getEntity() instanceof LivingEntity entity)
        {
            if (!FriendManager.INSTANCE.isFriend(entity) && entity.hurtTime == 0)
            {
                if (!doingAutoDtap)
                    beginAutoDtap(entity);
            }
        }
    }

    public void beginAutoDtap(LivingEntity entity)
    {
        if (!autoDtap.getValue()) return;

        target = entity;
        doingAutoDtap = true;
        didAutoDtapAttack = true;
        didAutoDtapCrystal = false;
        lethalDelay.resetDelay();
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

    }


    @Override
    public String getDescription()
    {
        return "LegitCrystal: cwhack crystal";
    }
}