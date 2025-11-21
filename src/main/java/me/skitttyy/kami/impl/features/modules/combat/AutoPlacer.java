package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.world.CollisionBoxEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.CrystalManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.utils.world.ProtectionUtils;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

public class AutoPlacer extends Module {
    public static AutoPlacer INSTANCE;

    Timer timer = new Timer();
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);
    public Value<Number> minDmg = new ValueBuilder<Number>()
            .withDescriptor("Min Damage")
            .withValue(7)
            .withRange(0, 36)
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Target Range")
            .withValue(5d)
            .withRange(1d, 10d)
            .register(this);
    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(6)
            .withRange(3, 6)
            .withPlaces(1)
            .register(this);
    Value<Number> blocksPerTick = new ValueBuilder<Number>()
            .withDescriptor("Blocks")
            .withValue(10)
            .withRange(1, 10)
            .withPlaces(0)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(false)
            .register(this);

    public Value<Boolean> render = new ValueBuilder<Boolean>()
            .withDescriptor("Render")
            .withValue(true)
            .register(this);
    public Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(255, 0, 0, 25))
            .withParentEnabled(true)
            .withParent(render)
            .register(this);
    public Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .withParentEnabled(true)
            .withParent(render)
            .register(this);
    public Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(200)
            .withRange(0, 1000)
            .withParentEnabled(true)
            .withParent(render)
            .register(this);

    public AutoPlacer() {
        super("AutoPlacer", Category.Combat);
        INSTANCE = this;
    }

    public Map<BlockPos, Long> renderPositions = new HashMap<>();

    Entity target;
    List<BlockPos> toPlace = new ArrayList<>();

    public Map<BlockPos, Long> placed = new HashMap<>();

    double startY = 0;

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event) {

        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "AutoPlacer"))
            return;


        if (AutoBreak.INSTANCE.didAction) return;

        if (getSlot() == -1) {
            return;
        }


        for (Map.Entry<BlockPos, Long> entry : placed.entrySet()) {
            BlockPos pos = entry.getKey();
            long time = entry.getValue();

            if (System.currentTimeMillis() - time > 200) {
                placed.remove(pos);
            }
        }

        toPlace.clear();
        int blocksInTick = 0;

        target = TargetUtils.getTarget(targetRange.getValue().floatValue());

        if (target != null) {

            BlockPos targetPos = getBestCrystalPlacePos((PlayerEntity) target);

            if (mc.world.getBlockState(targetPos).isReplaceable()) {
                placeLabel:
                if (timer.isPassed()) {

                    if (blocksInTick >= blocksPerTick.getValue().intValue()) break placeLabel;

                    if (toPlace.contains(targetPos)) break placeLabel;

                    if (placed.containsKey(targetPos)) {

                        if (CrystalManager.INSTANCE.isRecentlyBlocked(targetPos))
                            placed.remove(targetPos);
                        else if (System.currentTimeMillis() - placed.get(targetPos) < 60)
                            break placeLabel;
                    }

                    if (blocksInTick == 0 && rotate.getValue() && !AntiCheat.INSTANCE.protocol.getValue())
                        RotationUtils.doRotate(targetPos, strictDirection.getValue());

                    toPlace.add(targetPos);
                    PriorityManager.INSTANCE.lockUsageLock("AutoPlacer");
                    blocksInTick++;
                    timer.resetDelay();
                }
            }
        }
        if (blocksInTick == 0) {
            PriorityManager.INSTANCE.unlockUsageLock();
        }

        if (AntiCheat.INSTANCE.protocol.getValue())
            doPlace();

    }

    public BlockPos getBestCrystalPlacePos(PlayerEntity player) {
        BlockPos bestPos = null;
        double bestDMG = 0.5D;

        final List<BlockPos> sphere = BlockUtils.sphere(range.getValue().doubleValue() + 1.0f, mc.player.getBlockPos(), true, false);
        Set<BlockPos> keySet = new HashSet<>(Set.copyOf(placed.keySet()));
        keySet.addAll(toPlace);

        for (BlockPos pos : sphere) {

            BlockPos basePos = pos.down();


            //check if valid place spot too cuz if theres an obby already there not tryna spam obby like a retard
            if (!CrystalUtil.canPlaceCrystal(basePos, CatAura.INSTANCE.onePointTwelve.getValue()) && !BlockUtils.isReplaceable(basePos))
                continue;

            if (basePos.getY() >= player.getBlockPos().getY())
                continue;

            if(!CrystalUtil.canPlaceCrystalAir(basePos)) continue;

            if(BlockUtils.isBlockedOff(basePos) || BlockUtils.isBlockedOff(pos)) continue;

            if(BlockUtils.isReplaceable(basePos)) {
                if (!BlockUtils.canPlaceBlock(basePos, strictDirection.getValue(), keySet)) continue;
            }

            double distance = mc.player.getEyePos().squaredDistanceTo(new Vec3d(basePos.getX() + 0.5, basePos.getY() + 0.5, basePos.getZ() + 0.5));
            if (distance > MathUtil.square(range.getValue().doubleValue()))
                continue;

            double dmg = CrystalUtil.calculateDamage(player, pos.toCenterPos(), CatAura.INSTANCE.terrain.getValue(), CatAura.INSTANCE.getMiningIgnore());
            if (dmg < minDmg.getValue().doubleValue()) {
                continue;
            }

            if (dmg > bestDMG) {
                bestPos = basePos;
                bestDMG = dmg;
            }


        }
        return bestPos;
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Post event) {

        if (NullUtils.nullCheck()) return;

        if (!AntiCheat.INSTANCE.protocol.getValue())
            doPlace();
    }


    @SubscribeEvent
    public void onCollision(CollisionBoxEvent event) {
        if (NullUtils.nullCheck()) return;


        if (placed.containsKey(event.getPos())) {

            event.setCancelled(true);
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0, 0, 0, 1.0, 1.0, 1.0)));
        }
    }


    @Override
    public void onEnable() {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        startY = mc.player.getY();

        placed.clear();
    }


    @Override
    public void onDisable() {
        super.onDisable();
        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoPlacer"))
            PriorityManager.INSTANCE.unlockUsageLock();

        placed.clear();

    }

    boolean rotateFlag = false;


    public void doPlace() {
        if (NullUtils.nullCheck()) return;

        int blockSlot = getSlot();

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        for (BlockPos pos : toPlace) {

            if (blockSlot != mc.player.getInventory().selectedSlot) {
                InventoryUtils.switchToSlot(blockSlot);
                switched = true;
            }

            placed.put(pos, System.currentTimeMillis());


            if (rotate.getValue() && AntiCheat.INSTANCE.protocol.getValue())
                RotationUtils.doSilentRotate(pos, strictDirection.getValue());

            BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue(), placed.keySet()), !mc.player.getMainHandStack().getItem().equals(Items.ENDER_CHEST));


            if (render.getValue())
                renderPositions.put(pos, System.currentTimeMillis());

        }
        if (switched) {
            InventoryUtils.switchToSlot(oldSlot);
        }
        if ((!toPlace.isEmpty() && AntiCheat.INSTANCE.protocol.getValue() && rotate.getValue()) || rotateFlag) {
            RotationUtils.silentSync();
        }
        toPlace.clear();

    }


    int getSlot() {
        return InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            final BlockPos targetPos = packet.getPos();
            if (placed.containsKey(targetPos)) {
                placed.remove(targetPos);
            }
        }
    }

    @Override
    public String getDescription() {
        return "AutoPlacer: places obby in places to place crystals";
    }
}
