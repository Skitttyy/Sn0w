package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.FrameEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.HitboxManager;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.*;
import me.skitttyy.kami.api.utils.world.ca.BreakResult;
import me.skitttyy.kami.api.utils.world.ca.PlaceResult;
import me.skitttyy.kami.api.utils.world.ca.Result;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import me.skitttyy.kami.impl.features.modules.player.Blink;
import me.skitttyy.kami.impl.features.modules.player.Phase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static me.skitttyy.kami.api.utils.math.MathUtil.normalize;


public class CatAura extends Module {

    public static CatAura INSTANCE;

    Value<String> page = new ValueBuilder<String>()
            .withDescriptor("Page")
            .withValue("Render")
            .withModes("Render", "Calc", "Place", "Break", "Misc", "Timing")
            .withAction(s -> handlePage(s.getValue()))
            .register(this);
    Value<String> targetSorting = new ValueBuilder<String>()
            .withDescriptor("Sort")
            .withValue("Damage")
            .withModes("Damage", "Range")
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(7d)
            .withRange(3d, 20d)
            .register(this);
    Value<Number> placeRange = new ValueBuilder<Number>()
            .withDescriptor("Place Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> placeWallsRange = new ValueBuilder<Number>()
            .withDescriptor("Place Walls Range")
            .withValue(3d)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> placeDelay = new ValueBuilder<Number>()
            .withDescriptor("Place Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    public Value<Number> minDmg = new ValueBuilder<Number>()
            .withDescriptor("Min Damage")
            .withValue(2)
            .withRange(0, 36)
            .register(this);
    Value<Number> maxSelfDmg = new ValueBuilder<Number>()
            .withDescriptor("Max Self Damage")
            .withValue(4)
            .withRange(0, 36)
            .register(this);
    Value<Boolean> noSuicide = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Suicide")
            .withValue(false)
            .register(this);
    public Value<Number> lethalCrystals = new ValueBuilder<Number>()
            .withDescriptor("Lethal Crystals")
            .withValue(0)
            .withRange(0, 5)
            .withPlaces(0)
            .register(this);
    public Value<Boolean> onePointTwelve = new ValueBuilder<Boolean>()
            .withDescriptor("1.12")
            .withValue(false)
            .register(this);
    Value<Boolean> limit = new ValueBuilder<Boolean>()
            .withDescriptor("Limit")
            .withValue(false)
            .register(this);
    Value<Boolean> antiSurround = new ValueBuilder<Boolean>()
            .withDescriptor("AntiFeetPlace")
            .withValue(false)
            .register(this);
    Value<String> multiTask = new ValueBuilder<String>()
            .withDescriptor("MultiTask")
            .withValue("None")
            .withModes("None", "Soft", "Strong")
            .register(this);
    Value<String> miningIgnore = new ValueBuilder<String>()
            .withDescriptor("Mining")
            .withValue("None")
            .withModes("None", "Ignore", "StrictIgnore")
            .register(this);
    Value<Boolean> terrain = new ValueBuilder<Boolean>()
            .withDescriptor("Terrain")
            .withValue(false)
            .register(this);
    public Value<Boolean> armorAssume = new ValueBuilder<Boolean>()
            .withDescriptor("Assume")
            .withValue(false)
            .register(this);
    Value<Boolean> predict = new ValueBuilder<Boolean>()
            .withDescriptor("Boost")
            .withValue(false)
            .register(this);
    Value<String> autoSwitch = new ValueBuilder<String>()
            .withDescriptor("Auto Switch")
            .withValue("None")
            .withModes("None", "Normal", "Silent", "SilentBypass")
            .withAction(s -> handleSubsettingAutoSwitch(s.getValue()))
            .register(this);
    Value<Boolean> noGapSwitch = new ValueBuilder<Boolean>()
            .withDescriptor("Pause")
            .withValue(false)
            .register(this);
    Value<String> antiWeakness = new ValueBuilder<String>()
            .withDescriptor("AntiWeakness")
            .withValue("None")
            .withModes("None", "Normal", "Silent")
            .register(this);
    Value<Number> breakDelay = new ValueBuilder<Number>()
            .withDescriptor("Break Delay")
            .withValue(1)
            .withRange(0, 1000)
            .register(this);
    Value<Number> breakRange = new ValueBuilder<Number>()
            .withDescriptor("Break Range")
            .withValue(6)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Number> breakWallsRange = new ValueBuilder<Number>()
            .withDescriptor("Break Walls Range")
            .withValue(3d)
            .withPlaces(1)
            .withRange(1d, 6d)
            .register(this);
    Value<Number> ticksExisted = new ValueBuilder<Number>()
            .withDescriptor("Ticks Existed")
            .withValue(0.0f)
            .withRange(0.0f, 10.0f)
            .withPlaces(0)
            .register(this);
    Value<Boolean> autoDtap = new ValueBuilder<Boolean>()
            .withDescriptor("Lethal Tick")
            .withValue(false)
            .register(this);
    Value<Boolean> autoHit = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Hit")
            .withValue(false)
            .register(this);
    Value<Number> dtapDelay = new ValueBuilder<Number>()
            .withDescriptor("Crystal Delay")
            .withValue(300)
            .withRange(100, 500)
            .register(this);
    Value<Boolean> inhibit = new ValueBuilder<Boolean>()
            .withDescriptor("Inhibit")
            .withValue(false)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);
    Value<String> setDeadMode = new ValueBuilder<String>()
            .withDescriptor("Set Dead")
            .withValue("None")
            .withModes("None", "Ghost")
            .register(this);
    Value<String> swapWait = new ValueBuilder<String>()
            .withDescriptor("Swap Delay")
            .withValue("None")
            .withModes("None", "Semi", "Full")
            .register(this);
    Value<Number> swapWaitDelay = new ValueBuilder<Number>()
            .withDescriptor("Swap Wait Delay")
            .withValue(300)
            .withRange(50, 500)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<String> rotationsType = new ValueBuilder<String>()
            .withDescriptor("Type", "rotationsType")
            .withValue("Simple")
            .withModes("Simple", "NCP", "GrimAbuse", "MultiPoint", "Silent")
            .register(this);
    Value<String> timing = new ValueBuilder<String>()
            .withDescriptor("Timing")
            .withValue("Soft")
            .withModes("Soft", "Strict")
            .register(this);
    Value<String> sequence = new ValueBuilder<String>()
            .withDescriptor("Sequence")
            .withValue("None")
            .withModes("None", "Soft", "Strong")
            .register(this);
    public Value<Sn0wColor> fillColorS = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill Color")
            .withValue(new Sn0wColor(0, 0, 0, 100))
            .register(this);
    public Value<Sn0wColor> lineColorS = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Outline Color")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    public Value<Boolean> renderDamage = new ValueBuilder<Boolean>()
            .withDescriptor("Damage Text")
            .withValue(false)
            .register(this);
    public Value<Number> textScale = new ValueBuilder<Number>()
            .withDescriptor("Text Scale")
            .withValue(1.4)
            .withRange(1, 2)
            .register(this);
    public Value<String> renderMode = new ValueBuilder<String>()
            .withDescriptor("Type")
            .withValue("Normal")
            .withModes("Normal", "Fade", "Glide")
            .register(this);
    public Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(1000)
            .withRange(100, 2000)
            .withPlaces(0)
            .register(this);
    public Value<Boolean> futureFade = new ValueBuilder<Boolean>()
            .withDescriptor("Future")
            .withValue(false)
            .register(this);
    Value<Number> glideSpeed = new ValueBuilder<Number>()
            .withDescriptor("Glide Speed")
            .withValue(5)
            .withRange(1, 40)
            .register(this);
    Value<Number> openSpeed = new ValueBuilder<Number>()
            .withDescriptor("Open Speed")
            .withValue(5)
            .withRange(1, 40)
            .register(this);


    //rename this to crystalaura when we are done!!
    // Skitttyy: guess i never renamed it lol - 2024
    public CatAura() {
        super("CatAura", Category.Combat);
        INSTANCE = this;
    }

    boolean placedOnspawn = false;

    public final Map<Integer, Long> hitCrystals = new ConcurrentHashMap<>();
    public static List<Pair.BlockPairTime> oldPlacements = new ArrayList<>();
    protected final LinkedList<Long> brokeCrystals = new LinkedList<>();

    public BlockPos renderPos;
    public BlockPos calcPos;
    public BlockPos lastPos;
    float[] placeTargetRot = null;
    float[] breakTargetRot = null;

    boolean breaking = false;
    boolean swapStop = false;
    Entity swapStopCrystal = null;

    int CRYSTALS_PER_SECOND = 0;
    public PlayerEntity target;
    public boolean offhand;
    boolean placeFlag = false;
    boolean breakFlag = false;
    Timer placeTimer = new Timer();
    Timer breakTimer = new Timer();
    Timer swapTimer = new Timer();
    Timer autoDtapTimer = new Timer();
    Timer cooldown = new Timer();

    public Entity calcCrystal;
    public double renderDMG;
    boolean doingAutoDtap = false;
    private float timePassed;
    private BlockPos lastRenderPos;

    @SubscribeEvent
    public void onEntityRemove(EntityEvent.Remove event) {

        // crystal being removed from world
        if (event.getEntity() instanceof EndCrystalEntity) {
            if (hitCrystals.containsKey(event.getEntity().getId())) {
                brokeCrystals.add(System.currentTimeMillis());
                hitCrystals.remove(event.getEntity().getId());
            }
        }
    }

    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event) {
        if (NullUtils.nullCheck()) return;

        if (rotate.getValue())
            if (PriorityManager.INSTANCE.isUsageLocked()) return;


        if (isMultiTask())
            return;


        if (predict.getValue() && event.getEntity() instanceof EndCrystalEntity entity)
            doPredict(entity);

    }


    @SubscribeEvent(Priority.SUPER_FIRST)
    public void onPacket(PacketEvent.Send event) {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
            if (packet.getSelectedSlot() == RotationManager.INSTANCE.serverSlot) return;


            if (!swapWait.getValue().equals("None")) {
                StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
                StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);

                if (!antiWeakness.getValue().equals("None") && (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() <= weaknessEffect.getAmplifier())))
                    return;

                swapTimer.resetDelay();
            }
        }
    }


    public void doPredict(EndCrystalEntity entity) {
        if (mc.player.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) <= MathUtil.square(breakRange.getValue().doubleValue())) {


            if (autoDtap.getValue() && isDtapping()) return;

            final float targetDamage = CrystalUtil.calculateDamage(target, new Vec3d(entity.getX(), entity.getY(), entity.getZ()), terrain.getValue(), false);
            if ((targetDamage > minDmg.getValue().doubleValue()) || (lethalCrystals.getValue().intValue() != 0 && (targetDamage * lethalCrystals.getValue().floatValue() >= target.getHealth() + target.getAbsorptionAmount()))) {

//                if (mc.player.isSprinting() && AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
//                    mc.player.setSprinting(false);


                if (rotationsType.getValue().equals("Silent")) {
                    RotationUtils.doSilentRotate(RotationUtils.getRotationsTo(mc.player.getEyePos(), new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ())));
                    hasRotated = true;
                }

                PlayerInteractEntityC2SPacket interactentity = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
                PacketManager.INSTANCE.sendPacket(interactentity);

                mc.player.swingHand(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                mc.player.resetLastAttackedTicks();
                hitCrystals.put(entity.getId(), System.currentTimeMillis());
                breakTimer.resetDelay();
                AntiCheat.INSTANCE.handleMultiTask();

                if (doingAutoDtap)
                    didAutoDtapCrystal = true;


                if (sequence.getValue().equals("Strong")) {
                    int crystalSlot = InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL);
                    if (crystalSlot == -1 && !offhand) {
                        if (hasRotated) {
                            hasRotated = false;
                            RotationUtils.silentSync();
                        }
                        return;
                    }


                    if (calcPos != null) {
                        placedOnspawn = false;
                        placeCrystal();
                        placedOnspawn = true;
                        if (hasRotated) {
                            hasRotated = false;
                            RotationUtils.silentSync();
                        }
                    }
                }
            }
        }

    }

    long startTime = 0;

    boolean isMultiTask() {
        if (mc.player.isUsingItem()) {
            switch (multiTask.getValue()) {
                case "Soft":

                    if (!mc.player.getActiveHand().equals(Hand.OFF_HAND) && !offhand) {
                        return true;
                    }
                    break;

                case "Strong":
                    return true;
            }
        }
        return false;
    }

    private Box renderBB;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (NullUtils.nullCheck()) return;

        if (!renderMode.getValue().equals("Fade")) {
            if (target == null || renderPos == null) {
                return;
            }
        }

        if (!renderMode.getValue().equals("Fade") && !canRender())
            return;

        Color fillColor = fillColorS.getValue().getColor();
        Color lineColor = lineColorS.getValue().getColor();
        switch (renderMode.getValue()) {
            case "Normal":
                RenderUtil.renderBox(
                        RenderType.FILL,
                        new Box(renderPos),
                        fillColor,
                        fillColor
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        new Box(renderPos),
                        lineColor,
                        lineColor
                );
                if (renderDamage.getValue())
                    RenderBuffers.schedulePreRender(() ->
                    {
                        RenderUtil.drawText(String.format("%.1f", renderDMG), new Box(renderPos).getCenter(), textScale.getValue().floatValue());
                    });

                break;
            case "Glide":
//                System.exit(0);
                if (lastRenderPos == null || mc.player.squaredDistanceTo(renderBB.minX, renderBB.minY, renderBB.minZ) > MathUtil.square(targetRange.getValue().doubleValue())) {
                    lastRenderPos = renderPos;
                    renderBB = new Box(renderPos);
                    timePassed = 0.0f;
                    startTime = System.currentTimeMillis();
                }
                if (!lastRenderPos.equals(renderPos)) {
                    lastRenderPos = renderPos;
                    timePassed = 0.0f;
                }

                final double xDiff = renderPos.getX() - renderBB.minX;
                final double yDiff = renderPos.getY() - renderBB.minY;
                final double zDiff = renderPos.getZ() - renderBB.minZ;
                float movespeedlelell = glideSpeed.getValue().intValue() * 200;
//                2000f
                float decellerate = 0.8f;
                float multiplier = timePassed / movespeedlelell * decellerate;
                if (multiplier > 1.0f) {
                    multiplier = 1.0f;
                }
                renderBB = renderBB.offset(xDiff * (double) multiplier, yDiff * (double) multiplier, zDiff * (double) multiplier);


                RenderUtil.renderBox(
                        RenderType.FILL,
                        renderBB,
                        fillColor,
                        fillColor
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        renderBB,
                        lineColor,
                        lineColor
                );


                if (renderDamage.getValue())
                    RenderBuffers.schedulePreRender(() ->
                    {
                        RenderUtil.drawText(String.format("%.1f", renderDMG), renderBB.offset(0.0, 0, 0.0).getCenter(), textScale.getValue().floatValue());
                    });

                if (renderBB.equals(new Box(renderPos))) {
                    timePassed = 0.0f;
                } else {
                    timePassed += 50.0f;
                }

                break;
        }
    }


    public Result getTargetResult() {
        switch (targetSorting.getValue()) {
            case "Damage":
                List<Entity> targets = TargetUtils.getTargets(targetRange.getValue().doubleValue()).toList();
                Result bestResult = null;
                for (Entity target : targets) {
                    if (target == mc.player) continue;

                    Result currentResult = getResult((PlayerEntity) target);
                    if (bestResult == null) {
                        bestResult = currentResult;
                        continue;
                    }
                    if (currentResult.getDamage() > bestResult.getDamage()) {
                        bestResult = currentResult;
                    }
                }
                if (bestResult == null) {
                    reset();
                    return null;
                }
                return bestResult;
            case "Range":
                target = (PlayerEntity) TargetUtils.getTarget(targetRange.getValue().doubleValue());
                if (target == null) {
                    reset();
                    return null;
                }
                return getResult(target);
        }
        return null;
    }

    Direction overide = null;
    boolean didAutoDtapAttack = false;
    boolean didAutoDtapCrystal = false;
    float lastTime = 0;

    public void prepare() {
        offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;


        placeTimer.setDelay(placeDelay.getValue().longValue());
        breakTimer.setDelay(breakDelay.getValue().longValue());
        autoDtapTimer.setDelay(dtapDelay.getValue().longValue());

        swapTimer.setDelay(swapWaitDelay.getValue().longValue());
        cooldown.setDelay(800L);
        placeTargetRot = null;
        breakTargetRot = null;


        long nanoPre = System.nanoTime();
        Result result = getTargetResult();
        long nanoPost = System.nanoTime() - nanoPre;
        lastTime = (float) (nanoPost / 1E6);

        calcPos = null;
        calcCrystal = null;
        load(result);

        if (inhibit.getValue() && calcCrystal != null) {
            if (hitCrystals.containsKey(calcCrystal.getId())) {
                breakTimer.setDelay(breakDelay.getValue().longValue() + 100);
            }
        }
        if (!rotate.getValue()) return;


        overide = null;

//        if (calcPos != null)
//        {
//            if (strictDirection.getValue() && placeTargetRot != null)
//            {
//                Direction side = BlockUtils.getPlaceableSideCrystal(calcPos, strictDirection.getValue());
//                if (!side.equals(Direction.UP))
//                {
//                    float[] rots = RotationUtils.getBlockRotations(calcPos, Direction.UP);
//
//                    BlockHitResult bhr = RaytraceUtils.getBlockHitResult(rots[0], rots[1]);
//                    if (bhr != null && bhr.getType() != HitResult.Type.MISS)
//                    {
//                        if (bhr.getBlockPos().equals(calcPos))
//                        {
//                            overide = bhr.getSide();
//                            placeTargetRot = rots;
//                        }
//                    }
//                }
//            }
//        }


        if (autoSwitch.getValue().equals("None") && breakTargetRot == null) {
            if (!offhand && mc.player.getInventory().getMainHandStack().getItem() != Items.END_CRYSTAL) {
                return;
            }
        }


        if (target != null) {
            if (PlayerUtils.isBoostedByFirework() && AntiCheat.INSTANCE.strafeFix.getValue() && !target.isFallFlying())
                return;
        }

        if (!rotationsType.getValue().equals("Silent")) {
            if (placeTargetRot != null && breakTargetRot == null) {
                RotationUtils.setRotation(placeTargetRot);
            } else if (placeTargetRot == null && breakTargetRot != null) {
                RotationUtils.setRotation(breakTargetRot);
            } else if (placeTargetRot != null) {
                handleSequence();
            }
        }
    }

    boolean hasRotated = false;

    public boolean breakCrystal() {
        if (breakFlag) {
            breakFlag = false;
            return true;
        }

        if (autoDtap.getValue() && target != null && (calcCrystal != null || calcPos != null)) {
            if (doingAutoDtap) {

                if (didAutoDtapAttack && !didAutoDtapCrystal && !autoDtapTimer.isPassed()) {
                    return true;
                }
                if (target.hurtTime != 0 && didAutoDtapCrystal && didAutoDtapAttack) {
                    return true;
                } else if (didAutoDtapAttack && didAutoDtapCrystal) {
                    doingAutoDtap = false;
                    didAutoDtapAttack = false;
                    didAutoDtapCrystal = false;
                }
            }

        } else {
            if (autoDtapTimer.isPassed()) {
                doingAutoDtap = false;
                didAutoDtapAttack = false;
                didAutoDtapCrystal = false;
            }

        }

        if (calcCrystal != null) {

            if (rotate.getValue() && breakTargetRot == null) return true;

            if (breakTimer.isPassed()) {

                if (!swapWait.getValue().equals("None") && !swapTimer.isPassed()) return true;


                StatusEffectInstance weaknessEffect = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
                StatusEffectInstance strengthEffect = mc.player.getStatusEffect(StatusEffects.STRENGTH);
                boolean swapBack = false;
                int curSlot = mc.player.getInventory().selectedSlot;
                if (!antiWeakness.getValue().equals("None") && (weaknessEffect != null && (strengthEffect == null || strengthEffect.getAmplifier() <= weaknessEffect.getAmplifier()))) {
                    /**
                     * TODO: THIS
                     * if (bestWeapon != -1 && RotationManager.INSTANCE.getServerSlot() != getBestWeapon())
                     */

                    int bestWeapon = InventoryUtils.getSwordSlot();
                    if (bestWeapon != -1 && mc.player.getInventory().selectedSlot != bestWeapon) {
                        switch (antiWeakness.getValue()) {
                            case "Normal":
                                InventoryUtils.switchToSlot(bestWeapon);
                                break;
                            case "Silent":
                                InventoryUtils.switchToSlot(bestWeapon);
                                swapBack = true;
                                break;
                        }
                    }
                }


                if (rotationsType.getValue().equals("Silent")) {
                    RotationUtils.doSilentRotate(breakTargetRot);
                    hasRotated = true;
                }
                PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(calcCrystal, mc.player.isSneaking());
                PacketManager.INSTANCE.sendPacket(packet);
                mc.player.swingHand(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                mc.player.resetLastAttackedTicks();
                if (!mc.player.isOnGround())
                    mc.player.addCritParticles(calcCrystal);

                hitCrystals.put(calcCrystal.getId(), System.currentTimeMillis());
                AntiCheat.INSTANCE.handleMultiTask();

                if (swapBack)
                    InventoryUtils.switchToSlot(curSlot);


                breakTimer.resetDelay();
                if (setDeadMode.getValue().equals("Ghost")) {
                    mc.executeSync(() ->
                    {
                        mc.world.removeEntity(calcCrystal.getId(), Entity.RemovalReason.KILLED);
                    });
                }

                if (doingAutoDtap && didAutoDtapAttack && !didAutoDtapCrystal) {
                    didAutoDtapCrystal = true;
                }
                return false;
            }
            return true;
        } else {
            return true;
        }
    }


    boolean isDtapping() {


        if (doingAutoDtap) {
            if (didAutoDtapAttack && didAutoDtapCrystal && autoDtapTimer.isPassed()) {
                return false;
            }
            return true;

//            if (target == null) return false;
//
//
//            if (calcPos == null && calcCrystal == null) return false;
//
//
//            if (didAutoDtapAttack && !didAutoDtapCrystal && !autoDtapTimer.isPassed())
//            {
//                return true;
//            }
//            if (target.hurtTime != 0 && didAutoDtapCrystal && didAutoDtapAttack)
//            {
//                return true;
//            } else if (didAutoDtapAttack && didAutoDtapCrystal)
//            {
//                return false;
//            }
//
//            if(didAutoDtapAttack) return false;
        }
        return false;
    }

    public void placeCrystal() {

        if (placeFlag) {
            placeFlag = false;
            return;
        }

        if (placedOnspawn)
            return;

        if (calcPos != null) {

            if (rotate.getValue() && placeTargetRot == null) return;


            if (placeTimer.isPassed()) {
                if (swapWait.getValue().equals("Full") && !swapTimer.isPassed()) return;


                boolean doSilent = false;
                int crystalSlot = InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL);

                if (crystalSlot == -1 && !offhand) {
                    renderPos = null;
                    return;
                }

                if (!offhand && autoSwitch.getValue().equals("Normal")) {
                    if (noGapSwitch.getValue()) {
                        if (!PlayerUtils.isEatingGap()) {
                            InventoryUtils.switchToSlot(crystalSlot);
                        }
                    } else {
                        InventoryUtils.switchToSlot(crystalSlot);
                    }
                }

                if (!offhand && (autoSwitch.getValue().equals("Silent") || autoSwitch.getValue().equals("SilentBypass"))) {
                    if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem))
                        doSilent = true;
                }


                if (!autoSwitch.getValue().contains("Silent")) {
                    if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem) && !offhand) {
                        renderPos = null;
                        return;
                    }
                }

                int oldSlot = mc.player.getInventory().selectedSlot;


                if (doSilent) {
                    if (autoSwitch.getValue().equals("SilentBypass")) {
                        InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(crystalSlot), false);
                    } else {
                        InventoryUtils.switchToSlot(crystalSlot);
                    }
                }

                Direction side = BlockUtils.getPlaceableSideCrystal(calcPos, strictDirection.getValue());

                if (side != null) {
                    Vec3d vec = calcPos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
                    if (side.getAxis().isHorizontal()) {
                        vec = vec.add(0, 0.45, 0);
                    }


                    if (rotationsType.getValue().equals("Silent")) {
                        RotationUtils.doSilentRotate(placeTargetRot);
                        hasRotated = true;
                    }
                    BlockHitResult result = new BlockHitResult(vec, side, calcPos, false);

                    PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, result, id));
                    mc.player.swingHand(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                    if (doSilent) {
                        if (autoSwitch.getValue().equals("SilentBypass")) {
                            InventoryUtils.switchToBypass(InventoryUtils.hotbarToInventory(crystalSlot), true);
                        } else {
                            InventoryUtils.switchToSlot(oldSlot);
                        }
                    }

                    AntiCheat.INSTANCE.handleMultiTask();

                    lastPos = calcPos;
                    placeTimer.resetDelay();
                }

            }
            renderPos = calcPos;
            if (renderPos != null && renderMode.getValue().equals("Fade")) {
                oldPlacements.removeIf(pair -> pair.key().equals(renderPos));
                oldPlacements.add(new Pair.BlockPairTime(renderPos));
            }
        }
    }

    public PlaceResult getBestPlaceResult(PlayerEntity targetPlayer, Entity currentCalcCrystal) {

        BlockPos bestPos = null;
        BlockPos bestAntiFeetPlacePos = null;
        double bestAntiFeetPlaceDamage = 0.5D;

        final List<BlockPos> sphere = BlockUtils.sphere(placeRange.getValue().doubleValue() + 1.0f, mc.player.getBlockPos(), true, false);
        double bestDMG = 0.5D;

        for (final BlockPos pos : sphere) {
            if (CrystalUtil.canPlaceCrystal(pos, onePointTwelve.getValue())) {


                AntiFeetPlaceResult feetPlaceResult = handleBBCrystal(pos, lastPos, currentCalcCrystal);

                if (!feetPlaceResult.isPlaceAvailable()) continue;


                if (!checkPlace(pos, false)) continue;


                boolean doMiningIgnore = getMiningIgnore();
                final float targetDamage = CrystalUtil.calculateDamage(targetPlayer, crystalDamageVec(pos), terrain.getValue(), doMiningIgnore);
                if (bestDMG < targetDamage && targetDamage > minDmg.getValue().doubleValue()) {
                    final float selfDamage = CrystalUtil.calculateDamage(mc.player, crystalDamageVec(pos), terrain.getValue(), doMiningIgnore);

                    if (selfDamage > maxSelfDmg.getValue().doubleValue()) continue;

                    if ((selfDamage + 2 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && noSuicide.getValue()))
                        continue;


                    if (feetPlaceResult.isAntiFeetPlace()) {
                        bestAntiFeetPlaceDamage = targetDamage;
                        bestAntiFeetPlacePos = pos;
                    } else {
                        bestDMG = targetDamage;
                        bestPos = pos;
                    }
                }
            }
        }
        if (bestDMG == 0.5D) {
            bestDMG = 0;
        }

        if (bestPos == null && bestAntiFeetPlacePos != null) {
            bestPos = bestAntiFeetPlacePos;
            bestDMG = bestAntiFeetPlaceDamage;
        }
        return new PlaceResult(targetPlayer, bestDMG, bestPos, getPlaceRot(bestPos));
    }

    public boolean getMiningIgnore() {
        return switch (miningIgnore.getValue()) {
            case "Ignore" -> true;
            case "StrictIgnore" ->
                    offhand || (mc.player.getInventory().getMainHandStack().getItem() instanceof EndCrystalItem);
            default -> false;
        };
    }

    public Result getResult(PlayerEntity player) {
        BreakResult breakResult = getBestBreakResult(player);
        Entity currentCalcCrystal = breakResult.calcCrystal;
        return new Result(breakResult, getBestPlaceResult(player, currentCalcCrystal));
    }


    public boolean checkPlace(BlockPos pos, boolean ignore) {

        Direction side = ignore ? BlockUtils.getPlaceableSideCrystal(pos, strictDirection.getValue(), pos.up()) : BlockUtils.getPlaceableSideCrystal(pos, strictDirection.getValue());

        if (side == null)
            return false;

        Vec3d vec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);

        double distanceSq = vec.squaredDistanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        if (distanceSq > MathUtil.square(placeRange.getValue().doubleValue()))
            return false;


        boolean trace = BlockUtils.placeTrace(pos);
        if (trace && distanceSq > MathUtil.square(placeWallsRange.getValue().doubleValue()))
            return false;

        double crystalDistance = mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, 2.20000004768, 0));

        if (crystalDistance > breakRange.getValue().doubleValue())
            return false;

        Vec3d raytrace = pos.toCenterPos().add(0, 2.20000004768, 0);

        if (crystalDistance > breakWallsRange.getValue().doubleValue()) {
            BlockHitResult result = mc.world.raycast(new RaycastContext(mc.player.getEyePos(), raytrace, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            return result.getType() == HitResult.Type.MISS;
        }
        return true;
    }

    public void load(Result result) {

        target = result.target;

        if (InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL) != -1 || offhand) {
            if (result.calcPos != null) {
                calcPos = result.calcPos;
                placeTargetRot = result.placeRots;
                renderDMG = result.bestPlaceDMG;

            } else {
                if (result.bestPlaceDMG == 0) {
                    renderPos = null;
                }

            }
        } else {
            calcPos = null;
            renderPos = null;
            placeTargetRot = null;
            renderDMG = 0;
        }
        if (result.calcCrystal != null) {
            calcCrystal = result.calcCrystal;
            breakTargetRot = result.breakRots;
        }
    }


    public void strongSequence() {
        boolean breakPassed = breakTimer.isPassed();
        boolean placePassed = placeTimer.isPassed();

        if (calcPos == null && calcCrystal == null) {
            return;
        }

        if (breakPassed && !placePassed) {
            RotationUtils.setRotation(breakTargetRot);
        } else if (placePassed && !breakPassed) {
            RotationUtils.setRotation(placeTargetRot);
        } else {
            if (calcPos == null) {
                RotationUtils.setRotation(breakTargetRot);
                return;
            }

            if (calcCrystal == null || !placePassed) {
                RotationUtils.setRotation(placeTargetRot);
                return;
            }


            switch (rotationsType.getValue()) {
                case "NCP":
                    if (mc.world.getNonSpectatingEntities(Entity.class, new Box(calcPos.add(0, 1, 0))).contains(calcCrystal)) {
                        if (RaytraceUtils.isLookingResult(mc.player, calcCrystal, mc.player.getEyePos(), placeTargetRot, 6.0f) != null) {
                            RotationUtils.setRotation(placeTargetRot);
                        } else {
                            RotationUtils.setRotation(breakTargetRot);
                            placeFlag = true;
                        }
                    } else {
                        RotationUtils.setRotation(breakTargetRot);
                        placeFlag = true;
                    }
                    break;
                case "GrimAbuse":
                    RotationUtils.setRotation(breakTargetRot);
                    break;
                case "Simple":
                    if (mc.world.getNonSpectatingEntities(Entity.class, new Box(calcPos.add(0, 1, 0))).contains(calcCrystal)) {
                        RotationUtils.setRotation(placeTargetRot);
                    } else if (calcCrystal.getBlockPos().down().equals(calcPos)) {
                        RotationUtils.setRotation(placeTargetRot);
                    } else {
                        RotationUtils.setRotation(breakTargetRot);
                    }
                    break;
                case "MultiPoint":
                    if (mc.world.getNonSpectatingEntities(Entity.class, new Box(calcPos.add(0, 1, 0))).contains(calcCrystal)) {
                        RotationUtils.setRotation(placeTargetRot);
                    } else if (calcCrystal.getBlockPos().down().equals(calcPos)) {
                        RotationUtils.setRotation(placeTargetRot);
                    } else {
                        RotationUtils.setRotation(breakTargetRot);
                    }
                    break;
            }

        }
    }


    public Vec3d crystalDamageVec(BlockPos pos) {
        return Vec3d.of(pos).add(0.5, 1.0, 0.5);
    }

    public float[] getPlaceRot(BlockPos pos) {
        if (pos == null) return null;

        if (!rotate.getValue()) return null;


        Direction side = BlockUtils.getPlaceableSideCrystal(pos, strictDirection.getValue());


        Vec3d vec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        if (side.getAxis().isHorizontal()) {
            vec = vec.add(0, 0.45, 0);
        }

        return RotationUtils.getRotationsTo(mc.player.getEyePos(), vec);
    }


    public void handleSequence() {
        switch (sequence.getValue()) {
            case "None":
                RotationUtils.setRotation(breakTargetRot);
                placeFlag = true;
                break;
            case "Soft", "Strong":
                strongSequence();
                break;
        }
    }

    public void reset() {
        calcCrystal = null;
        calcPos = null;
        breaking = false;
        breakTargetRot = null;
        placeTargetRot = null;
    }

    public AntiFeetPlaceResult handleBBCrystal(BlockPos pos, BlockPos lastPos, Entity currentCalcCrystal) {
        Box bb = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);

        BlockPos pos2 = pos.up();
        double d = pos2.getX();
        double e = pos2.getY();
        double f = pos2.getZ();
        bb = new Box(d, e, f,
                d + bb.maxX, e + bb.maxY, f + bb.maxZ);

        boolean isAntiFeetPlace = false;
        if (lastPos != null && lastPos.equals(pos)) {
            for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, bb)) {
                if (entity instanceof EndCrystalEntity) {
                    if (!entity.isAlive() || entity.getBlockPos().down().equals(pos) || entity.equals(currentCalcCrystal)) {
                        continue;
                    }
                }

                if (antiSurround.getValue() && entity instanceof ItemEntity) {
                    if (AutoBreak.INSTANCE.isInstantMining(entity.getBlockPos())) {
                        isAntiFeetPlace = true;
                        continue;
                    }
                }

                if (entity instanceof PlayerEntity player) {
                    if (HitboxManager.INSTANCE.isServerCrawling(player)) {
                        Box serverBB = HitboxManager.INSTANCE.getCrawlingBoundingBox(player);

                        if (!bb.intersects(serverBB)) continue;
                    }
                }
                if (!entity.isAlive())
                    continue;

                return new AntiFeetPlaceResult(false, isAntiFeetPlace);
            }
        } else {
            for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, bb)) {

                if (entity instanceof PlayerEntity player) {
                    if (HitboxManager.INSTANCE.isServerCrawling(player)) {
                        Box serverBB = HitboxManager.INSTANCE.getCrawlingBoundingBox(player);

                        if (!bb.intersects(serverBB)) continue;
                    }
                }

                if (antiSurround.getValue() && entity instanceof ItemEntity) {
                    if (AutoBreak.INSTANCE.isInstantMining(entity.getBlockPos())) {
                        isAntiFeetPlace = true;
                        continue;
                    }
                }

                if (!entity.isAlive() || entity.equals(currentCalcCrystal))
                    continue;


                return new AntiFeetPlaceResult(false, isAntiFeetPlace);
            }
        }
        return new AntiFeetPlaceResult(true, isAntiFeetPlace);
    }


    public BreakResult getBestBreakResult(PlayerEntity targetPlayer) {
        Entity bestCrystal = null;
        double maxDamage = 0.5D;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
                if (canBreakCrystal(crystal)) {
                    final float targetDamage = CrystalUtil.calculateDamage(targetPlayer, entity.getPos(), terrain.getValue(), false);

                    // (isArmorBreaker(targetPlayer) && targetDamage >= minDmgArmor.getloatValue())
                    if (maxDamage < targetDamage && (targetDamage > minDmg.getValue().doubleValue()) || (lethalCrystals.getValue().intValue() != 0 && (targetDamage * lethalCrystals.getValue().floatValue() >= targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount()))) {
                        final float selfDamage = CrystalUtil.calculateDamage(mc.player, entity.getPos(), terrain.getValue(), false);

                        if (selfDamage > maxSelfDmg.getValue().doubleValue()) continue;

                        if ((selfDamage + 2 > mc.player.getHealth() + mc.player.getAbsorptionAmount() && noSuicide.getValue()))
                            continue;


                        maxDamage = targetDamage;
                        bestCrystal = entity;

                    }
                }
            }
        }

        float[] breakRots = null;
        if (bestCrystal != null) {
            if (rotate.getValue())
                breakRots = RotationUtils.getRotationsTo(mc.player.getEyePos(), new Vec3d(bestCrystal.getX(), bestCrystal.getEyeY(), bestCrystal.getZ()));
        }
        return new BreakResult(targetPlayer, maxDamage, bestCrystal, breakRots);
    }

    public boolean canRender() {
        if (calcPos == null) return false;


        int crystalSlot = InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL);
        return crystalSlot != -1 || offhand;
    }


    public boolean canBreakCrystal(EndCrystalEntity entity) {


        double distance = mc.player.getEyePos().distanceTo(entity.getPos().add(0, 1.700000047683716, 0));

        if (rotationsType.getValue().equals("MultiPoint")) {
            if (entity.getBlockPos().getY() > mc.player.getBlockPos().getY()) {
                distance = mc.player.getEyePos().distanceTo(entity.getPos().add(0, 0.5f, 0));
            }
        }


        if (ticksExisted.getValue().intValue() != 0) {
            if (entity.age <= ticksExisted.getValue().intValue())
                return false;
        }

        if (distance > breakRange.getValue().doubleValue()) return false;


        return (PlayerUtils.canEntityBeSeen(entity) || distance <= breakWallsRange.getValue().doubleValue());
    }

    public void handleSubsettingAutoSwitch(String value) {
        noGapSwitch.setActive(value.equals("Normal") && page.getValue().equals("Place"));
    }

    public void handlePage(String page) {
        // "Render", "Calc", "Place", "Break", "Timing", "Dev"
        // Render
        fillColorS.setActive(page.equals("Render"));
        lineColorS.setActive(page.equals("Render"));
        renderDamage.setActive(page.equals("Render"));
        textScale.setActive(page.equals("Render") && renderDamage.getValue());

        renderMode.setActive(page.equals("Render"));
        fadeTime.setActive(page.equals("Render") && renderMode.getValue().equals("Fade"));
        futureFade.setActive(page.equals("Render") && renderMode.getValue().equals("Fade"));
        openSpeed.setActive(page.equals("Render") && renderMode.getValue().equals("Glide"));
        glideSpeed.setActive(page.equals("Render") && renderMode.getValue().equals("Glide"));

        // Calc
        minDmg.setActive(page.equals("Calc"));
        maxSelfDmg.setActive(page.equals("Calc"));
        targetRange.setActive(page.equals("Calc"));
        targetSorting.setActive(page.equals("Calc"));

        noSuicide.setActive(page.equals("Calc"));
        lethalCrystals.setActive(page.equals("Calc"));
        multiTask.setActive(page.equals("Calc"));
        antiSurround.setActive(page.equals("Calc"));
        miningIgnore.setActive(page.equals("Calc"));
        terrain.setActive(page.equals("Calc"));
        armorAssume.setActive(page.equals("Calc"));


        // Place
        placeRange.setActive(page.equals("Place"));
        placeWallsRange.setActive(page.equals("Place"));

        autoSwitch.setActive(page.equals("Place"));
        noGapSwitch.setActive(page.equals("Place") && autoSwitch.getValue().equals("Normal"));
        strictDirection.setActive(page.equals("Place"));

        onePointTwelve.setActive(page.equals("Place"));
        limit.setActive(page.equals("Place"));

        //break
        breakRange.setActive(page.equals("Break"));
        breakWallsRange.setActive(page.equals("Break"));
        autoDtap.setActive(page.equals("Break"));
        autoHit.setActive(page.equals("Break") && autoDtap.getValue());

        dtapDelay.setActive(page.equals("Break") && autoDtap.getValue());

        ticksExisted.setActive(page.equals("Break"));

        setDeadMode.setActive(page.equals("Break"));
        predict.setActive(page.equals("Break"));

        antiWeakness.setActive(page.equals("Break"));

        inhibit.setActive(page.equals("Break"));

        //misc
        rotate.setActive(page.equals("Misc"));
        rotationsType.setActive(page.equals("Misc") && rotate.getValue());


        swapWait.setActive(page.equals("Misc"));
        swapWaitDelay.setActive(page.equals("Misc") && !(swapWait.getValue().equals("None")));

        // delays
        breakDelay.setActive(page.equals("Timing"));
        placeDelay.setActive(page.equals("Timing"));
        timing.setActive(page.equals("Timing"));

        sequence.setActive(page.equals("Timing"));

    }

    @SubscribeEvent
    public void onFrameFlip(FrameEvent.FrameFlipEvent event) {
        long currentTimeMs = System.currentTimeMillis();

        if (!brokeCrystals.isEmpty()) {
            try {
                while (true) {
                    if (brokeCrystals.isEmpty()) break;

                    long firstCrystal = brokeCrystals.getFirst();
                    final long second = 1000L;
                    if (currentTimeMs - firstCrystal > second) brokeCrystals.remove();
                    else break;
                }
            } catch (Exception e) {
                CRYSTALS_PER_SECOND = 0;
            }
        }
        CRYSTALS_PER_SECOND = brokeCrystals.size();
    }


    @SubscribeEvent(Priority.MODULE_LAST)
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event) {
        if (NullUtils.nullCheck()) return;

        if (Phase.INSTANCE.isEnabled()) return;


        if (rotate.getValue())
            if (PriorityManager.INSTANCE.isUsageLocked()) return;

        if (Blink.INSTANCE.isEnabled()) return;

        if (rotate.getValue())
            if (AutoBreak.INSTANCE.didAction) return;


        if (isMultiTask()) {
            reset();
            return;
        }

        prepare();


        if (autoDtap.getValue() && autoHit.getValue() && calcPos == null && !mc.player.isUsingItem() && cooldown.isPassed()) {
            if (HoleUtils.isSurrounded(target.getBlockPos()) && !HoleUtils.isSurrounded(target.getBlockPos().up()) && mc.world.getBlockState(target.getBlockPos().up(2)).isAir()) {
                if (!didAutoDtapAttack && target.hurtTime == 0 && !doingAutoDtap && KillAura.INSTANCE.isInAttackRange(mc.player.getEyePos(), target)) {
                    int oldSlot = mc.player.getInventory().selectedSlot;
                    int bestWeapon = InventoryUtils.getSwordSlot();
                    boolean switched = false;
                    if (bestWeapon != -1 && mc.player.getInventory().selectedSlot != bestWeapon) {
                        InventoryUtils.switchToSlot(bestWeapon);
                        switched = true;
                    }
                    if (rotate.getValue()) {
                        float[] rotation = RotationUtils.getRotationsTo(mc.player.getEyePos(), KillAura.INSTANCE.getAttackRotateVec(target));
                        RotationUtils.setRotation(rotation);
                    }
                    PlayerUtils.attackTarget(target);
                    if (switched) {
                        InventoryUtils.switchToSlot(oldSlot);
                    }
                    beginAutoDtap();
                    cooldown.resetDelay();
                }
            }
        }


        if (timing.getValue().equals("Soft"))
            interact();
    }


    public static void changeId(PlayerInteractEntityC2SPacket packet, int id) {
        try {
            Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField(FabricLoader.getInstance().isDevelopmentEnvironment() ? "entityId" : "field_12870");
            field.setAccessible(true);
            field.setInt(packet, id);
        } catch (Exception ignored) {
        }
    }

    public void beginAutoDtap() {
        if (!autoDtap.getValue()) return;


        doingAutoDtap = true;
        didAutoDtapAttack = true;
        didAutoDtapCrystal = false;
        autoDtapTimer.resetDelay();
        breakFlag = true;
    }

    @SubscribeEvent(Priority.MODULE_LAST)
    public void onPlayerUpdate(TickEvent.MovementTickEvent.Post event) {
        if (NullUtils.nullCheck()) return;

        if (Phase.INSTANCE.isEnabled()) return;

        if (rotate.getValue())
            if (PriorityManager.INSTANCE.isUsageLocked()) return;


        if (Blink.INSTANCE.isEnabled()) return;

        if (rotate.getValue())
            if (AutoBreak.INSTANCE.didAction) return;


        if (isMultiTask())
            return;

        if (timing.getValue().equals("Strict"))
            interact();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        renderPos = null;
        target = null;
        swapTimer.setDelay(swapWaitDelay.getValue().longValue());
        calcPos = null;
        calcCrystal = null;
        breaking = false;
        swapStopCrystal = null;
        swapStop = false;
        breakTargetRot = null;
        placeTargetRot = null;

    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        renderPos = null;
        target = null;
        swapTimer.setDelay(swapWaitDelay.getValue().longValue());
        calcPos = null;
        calcCrystal = null;
        breaking = false;
        swapStopCrystal = null;
        swapStop = false;
        breakTargetRot = null;
        placeTargetRot = null;

    }

    public void interact() {


        boolean breakCrystal = breakCrystal();


        int crystalSlot = InventoryUtils.getHotbarItemSlot(Items.END_CRYSTAL);
        if (crystalSlot != -1 || offhand) {
            if (!limit.getValue() || (!breakCrystal || calcCrystal == null))
                placeCrystal();
        }

        if (placedOnspawn)
            placedOnspawn = false;


        if (hasRotated) {
            RotationUtils.silentSync();
            hasRotated = false;
        }
    }

    String lastTargetName = null;

    @Override
    public String getHudInfo() {
        if (target == null && lastTargetName == null)
            return "0.0ms" + Formatting.GRAY + ", " + Formatting.WHITE + "1" + Formatting.GRAY + ", " + Formatting.WHITE + (new DecimalFormat("0.0").format(renderDMG)) + Formatting.GRAY + ", " + Formatting.WHITE + CRYSTALS_PER_SECOND;

        if (target != null)
            lastTargetName = target.getName().getString();
        return new DecimalFormat("0.##").format(lastTime) + "ms" + Formatting.GRAY + ", " + Formatting.WHITE + ((calcPos != null ? breakTimer.getResetTime() : 1) + "" + Formatting.GRAY + ", " + Formatting.WHITE + (new DecimalFormat("0.##").format(renderDMG)) + Formatting.GRAY + ", " + Formatting.WHITE + CRYSTALS_PER_SECOND);


    }

    @Override
    public String getDescription() {
        return "CatAura: Places and breaks crystals to kill players";
    }


}
