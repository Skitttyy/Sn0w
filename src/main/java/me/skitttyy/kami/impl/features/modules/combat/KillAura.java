package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.HitboxManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.management.TPSManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.targeting.EntityTargeter;
import me.skitttyy.kami.api.utils.targeting.Sorting;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

public class KillAura extends Module
{
    public static KillAura INSTANCE;
    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(3, 6)
            .withPlaces(1)
            .register(this);
    public Value<Number> wallsRange = new ValueBuilder<Number>()
            .withDescriptor("Walls Range")
            .withValue(3)
            .withRange(3, 6)
            .withPlaces(1)
            .register(this);
    Value<String> sorting = new ValueBuilder<String>()
            .withDescriptor("Sorting")
            .withValue("Distance")
            .withModes("Distance", "FOV")
            .register(this);
    Value<String> multiTask = new ValueBuilder<String>()
            .withDescriptor("MultiTask")
            .withValue("None")
            .withModes("None", "Soft", "Strong")
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
    Value<String> delay = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Cooldown")
            .withModes("Cooldown", "Delay")
            .register(this);
    public Value<Number> attackSpeed = new ValueBuilder<Number>()
            .withDescriptor("Attack Speed")
            .withValue(20)
            .withRange(0.1, 20)
            .withPlaces(1)
            .withPageParent(delay)
            .withPage("Delay")
            .register(this);
    Value<String> tpsSync = new ValueBuilder<String>()
            .withDescriptor("TPS Sync")
            .withValue("None")
            .withModes("None", "Average", "Last")
            .withPageParent(delay)
            .withPage("Cooldown")
            .register(this);
    Value<Boolean> autoSwitch = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Switch")
            .withValue(false)
            .register(this);
    Value<Boolean> noGapSwitch = new ValueBuilder<Boolean>()
            .withDescriptor("Eat Pause")
            .withValue(false)
            .withParentEnabled(true)
            .withParent(autoSwitch)
            .register(this);
    Value<Boolean> onlySword = new ValueBuilder<Boolean>()
            .withDescriptor("Only Weapon")
            .withValue(true)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> yawStep = new ValueBuilder<Boolean>()
            .withDescriptor("Yaw Step")
            .withValue(true)
            .withParent(rotate)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> silent = new ValueBuilder<Boolean>()
            .withDescriptor("Persist")
            .withValue(true)
            .withParent(rotate)
            .withParentEnabled(true)
            .register(this);
    Value<String> rotateType = new ValueBuilder<String>()
            .withDescriptor("Type")
            .withValue("Auto")
            .withModes("Auto", "Head", "Torso", "Feet")
            .withParent(rotate)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> esp = new ValueBuilder<Boolean>()
            .withDescriptor("ESP")
            .withValue(false)
            .register(this);
    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(255, 62, 62, 25))
            .withParent(esp)
            .withParentEnabled(true)
            .register(this);
    Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 62, 62, 255))
            .withParent(esp)
            .withParentEnabled(true)
            .register(this);

    public KillAura()
    {
        super("KillAura", Category.Combat);
        INSTANCE = this;
    }

    Timer hitTimer = new Timer();
    public Entity target;

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;


        if (PriorityManager.INSTANCE.isUsageLocked()) return;

        hitTimer.setDelayCPS(attackSpeed.getValue().floatValue());

        ArrayList<EntityType> toTarget = new ArrayList<>();
        if (players.getValue())
            toTarget.add(EntityType.PLAYER);

        if (mobs.getValue())
            toTarget.add(EntityType.ZOMBIE);

        if (animals.getValue())
            toTarget.add(EntityType.PIG);

        if (vehicles.getValue())
            toTarget.add(EntityType.MINECART);

        Entity newTarget = new EntityTargeter(mc.player, sorting.getValue().equals("Distance") ? Sorting.DISTANCE : Sorting.FOV, range.getValue().floatValue(), toTarget).findTarget(mc.player.getEyePos());
        if (newTarget == null)
        {
            target = null;
            return;
        }

        if (CatAura.INSTANCE.isEnabled() && (CatAura.INSTANCE.calcCrystal != null || CatAura.INSTANCE.calcPos != null))
        {
            target = null;
            return;
        }

        if (onlySword.getValue()
                && !(mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof MaceItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem))
        {
            target = null;
            return;
        } else
        {
            if (autoSwitch.getValue() && (!noGapSwitch.getValue() || !PlayerUtils.isEatingGap()))
                equipBestWeapon();
        }
        target = newTarget;
        if (!isInAttackRange(mc.player.getEyePos(), target)) return;


        if (mc.player.isUsingItem())
        {
            switch (multiTask.getValue())
            {
                case "Soft":
                    if (!mc.player.getActiveHand().equals(Hand.OFF_HAND))
                    {
                        target = null;
                        return;
                    }
                    break;
                case "Strong":
                    target = null;
                    return;
            }
        }


        boolean canAttack = false;
        int bestWeapon = InventoryUtils.getSwordSlot();

        if (delay.getValue().equals("Cooldown"))
        {
            float ticks = 20.0f;

            switch (tpsSync.getValue())
            {
                case "None":
                    ticks -= 20.0f;
                    break;
                case "Last":
                    ticks -= TPSManager.INSTANCE.getTickRate();
                    break;
                case "Average":
                    ticks -= TPSManager.INSTANCE.getAverage();
                    break;
            }
            float progress = mc.player.getAttackCooldownProgress(ticks);
            canAttack = progress >= 1.0f;
        } else if (hitTimer.isPassed())
        {
            canAttack = true;
            hitTimer.resetDelay();
        }

        if (rotate.getValue() && (silent.getValue() || canAttack))
        {


            if (target instanceof PlayerEntity entity)
            {
                if (PlayerUtils.isBoostedByFirework() && AntiCheat.INSTANCE.strafeFix.getValue() && !entity.isFallFlying())
                    return;
            }

            float[] rotation = RotationUtils.getRotationsTo(mc.player.getEyePos(), getAttackRotateVec(target));
            RotationUtils.setRotation(rotation);
        }

        if (canAttack)
        {
            if (target instanceof PlayerEntity target)
            {
                if (CatAura.INSTANCE.lastTargetName != null)
                {
                    if (CatAura.INSTANCE.lastTargetName.equals(target.getName().getString()))
                    {
                        if (!CatAura.INSTANCE.didAutoDtapAttack)
                        {
                            if (target.hurtTime == 0 && !CatAura.INSTANCE.doingAutoDtap)
                            {
                                CatAura.INSTANCE.beginAutoDtap();
                            }
                        }
                    }
                }
            }
            PlayerUtils.attackTarget(target);


        }

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (PriorityManager.INSTANCE.isUsageLocked()) return;

        if (!esp.getValue()) return;

        if (target != null)
        {
            Box bb = Interpolator.getInterpolatedEntityBox(target);
            RenderUtil.renderBox(
                    RenderType.FILL,
                    bb,
                    fill.getValue().getColor(),
                    fill.getValue().getColor()
            );

            RenderUtil.renderBox(
                    RenderType.LINES,
                    bb,
                    line.getValue().getColor(),
                    line.getValue().getColor()
            );
        }
    }

    public static void equipBestWeapon()
    {
        int slot = InventoryUtils.getSwordSlot();

        if (slot == -1 || slot == mc.player.getInventory().selectedSlot) return;

        InventoryUtils.switchToSlot(slot);
    }


    public boolean isInAttackRange(Vec3d pos, Entity entity)
    {
        final Vec3d entityPos = getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return isInAttackRange(dist, pos, entityPos);
    }

    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos)
    {
        if (dist > range.getValue().floatValue())
        {
            return false;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                pos, entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || !(dist > wallsRange.getValue().floatValue());
    }

    public Vec3d getAttackRotateVec(Entity entity)
    {
        Vec3d feetPos = entity.getPos();
        return switch (rotateType.getValue())
        {
            case "Feet" -> feetPos;
            case "Torso" -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case "Head" -> entity.getEyePos();
            case "Auto" ->
            {
                Vec3d torsoPos = feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
                Vec3d eyesPos = entity.getEyePos();
                if (mc.player.getEyePos().squaredDistanceTo(eyesPos) < 0.2f)
                {
                    yield feetPos;
                }
                yield Stream.of(feetPos, torsoPos, eyesPos).min(Comparator.comparing(b -> mc.player.getEyePos().squaredDistanceTo(b))).orElse(eyesPos);
            }
            default -> throw new IllegalStateException("Unexpected value: " + rotateType.getValue());
        };
    }


    @Override
    public String getHudInfo()
    {
        return "Single";
    }

    @Override
    public String getDescription()
    {
        return "KillAura: Clicks on opps";
    }
}
