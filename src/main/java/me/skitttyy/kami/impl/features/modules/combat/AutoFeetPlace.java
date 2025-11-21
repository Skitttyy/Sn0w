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
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.utils.world.ProtectionUtils;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

public class AutoFeetPlace extends Module
{
    public static AutoFeetPlace INSTANCE;

    Timer timer = new Timer();
    Timer hitcrystalCooldown = new Timer();


    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Dynamic")
            .withModes("Dynamic", "Feet")
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);
    Value<Boolean> adaptiveBox = new ValueBuilder<Boolean>()
            .withDescriptor("Adaptive")
            .withValue(true)
            .register(this);
    Value<Boolean> breakCrystal = new ValueBuilder<Boolean>()
            .withDescriptor("Break")
            .withValue(true)
            .register(this);
    Value<Boolean> predict = new ValueBuilder<Boolean>()
            .withDescriptor("Predict")
            .withValue(false)
            .withParent(breakCrystal)
            .withParentEnabled(true)
            .register(this);
    Value<Number> breakDelay = new ValueBuilder<Number>()
            .withDescriptor("Break Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> hitcrystalCooldown.setDelay(set.getValue().longValue()))
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
    Value<Boolean> sequential = new ValueBuilder<Boolean>()
            .withDescriptor("Overplace")
            .withValue(false)
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(false)
            .register(this);
    Value<Boolean> jumpDisable = new ValueBuilder<Boolean>()
            .withDescriptor("Disable")
            .withValue(true)
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
    public Map<BlockPos, Long> renderPositions = new HashMap<>();

    public AutoFeetPlace()
    {
        super("AutoFeetPlace", Category.Combat);
        INSTANCE = this;
    }

    public Map<BlockPos, Long> placed = new HashMap<>();

    List<BlockPos> toPlace = new ArrayList<>();
    double startY = 0;
    Timer past = new Timer();

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;

        past.setDelay(600);
        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "AutoFeetPlace"))
            return;


        if(AutoBreak.INSTANCE.didAction) return;

        if (getSlot() == -1)
        {
            if (past.isPassed())
                this.setEnabled(false);
            return;
        } else
        {
            past.resetDelay();
        }

        if (jumpDisable.getValue() && (!mc.player.isInLava() && !mc.player.isSubmergedInWater()) && (Math.abs(mc.player.getY() - startY) > (mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL) ? 0.5 : 0.2)))
        {

            setEnabled(false);
            return;
        }

        for (Map.Entry<BlockPos, Long> entry : placed.entrySet())
        {
            BlockPos pos = entry.getKey();
            long time = entry.getValue();

            if (System.currentTimeMillis() - time > 200)
            {
                placed.remove(pos);
            }
        }

        toPlace.clear();
        int blocksInTick = 0;

        if (timer.isPassed())
        {

            for (BlockPos targetPos : ProtectionUtils.getSurroundPlacements(false, mode.getValue().equals("Dynamic"), adaptiveBox.getValue(), !canOverplace(), strictDirection.getValue()))
            {

                if (blocksInTick >= blocksPerTick.getValue().intValue()) break;


                if (toPlace.contains(targetPos)) continue;


                Set<BlockPos> keySet = new HashSet<>(Set.copyOf(placed.keySet()));
                keySet.addAll(toPlace);

                if (!BlockUtils.canPlaceBlock(targetPos, strictDirection.getValue(), keySet)) continue;

                if (placed.containsKey(targetPos))
                {

                    if (CrystalManager.INSTANCE.isRecentlyBlocked(targetPos))
                        placed.remove(targetPos);
                    else if (System.currentTimeMillis() - placed.get(targetPos) < 60)
                        continue;
                }

                if (blocksInTick == 0 && rotate.getValue() && !AntiCheat.INSTANCE.protocol.getValue())
                    RotationUtils.doRotate(targetPos, strictDirection.getValue());

                toPlace.add(targetPos);
                PriorityManager.INSTANCE.lockUsageLock("AutoFeetPlace");
                blocksInTick++;
                timer.resetDelay();
            }

        }
        if (blocksInTick == 0)
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }

        doAttack();

        if (AntiCheat.INSTANCE.protocol.getValue())
            doPlace();

    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Post event)
    {

        if (NullUtils.nullCheck()) return;

        if (!AntiCheat.INSTANCE.protocol.getValue())
            doPlace();
    }

    boolean hitCrystal(BlockPos pos)
    {
        if (!hitcrystalCooldown.isPassed()) return false;

        for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)))
        {
            if (CrystalUtil.isEndCrystal(entity) && breakCrystal.getValue() && entity.isAlive())
            {
                if (CatAura.INSTANCE.rotate.getValue())
                {
                    float[] rots = new float[]{RotationUtils.getActualYaw(), RotationUtils.getActualPitch()};


                    if (RotationManager.INSTANCE.isRotating())
                    {
                        if (!RotationManager.INSTANCE.requests.isEmpty())
                        {
                            Rotation rotation = RotationManager.INSTANCE.getRotationRequest();
                            rots = new float[]{rotation.getYaw(), rotation.getPitch()};
                        } else
                        {
                            rots = new float[]{RotationManager.INSTANCE.getRotation().getYaw(), RotationManager.INSTANCE.getRotation().getPitch()};
                        }
                    }

                    if (RaytraceUtils.isLookingResult(mc.player, entity, mc.player.getEyePos(), rots, 6.0f) == null)
                    {
                        float[] breakRots = RotationUtils.getRotationsTo(mc.player.getEyePos(), new Vec3d(entity.getX(), entity.getY(), entity.getZ()));

                        if (AntiCheat.INSTANCE.protocol.getValue())
                        {
                            RotationUtils.doSilentRotate(breakRots);
                            rotateFlag = true;
                        } else if (AntiCheat.INSTANCE.acMode.getValue().equals("Soft"))
                        {
                            RotationUtils.packetRotate(breakRots);
                        }
                    }
                }
                PlayerUtils.attackTarget(entity);
                hitcrystalCooldown.resetDelay();
                return true;
            }
        }
        return false;

    }

    @SubscribeEvent
    public void onCollision(CollisionBoxEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (placed.containsKey(event.getPos()))
        {

            event.setCancelled(true);
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0, 0, 0, 1.0, 1.0, 1.0)));
        }
    }


    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        startY = mc.player.getY();

        placed.clear();
    }


    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoFeetPlace"))
            PriorityManager.INSTANCE.unlockUsageLock();

        placed.clear();

    }

    boolean rotateFlag = false;


    public void doAttack()
    {
        if (NullUtils.nullCheck()) return;

        for (BlockPos pos : toPlace)
        {
            if (hitCrystal(pos)) break;
        }
    }

    public void doPlace()
    {
        if (NullUtils.nullCheck()) return;

        int blockSlot = getSlot();

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        for (BlockPos pos : toPlace)
        {

            if (blockSlot != mc.player.getInventory().selectedSlot)
            {
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
        if (switched)
        {
            InventoryUtils.switchToSlot(oldSlot);
        }
        if ((!toPlace.isEmpty() && AntiCheat.INSTANCE.protocol.getValue() && rotate.getValue()) || rotateFlag)
        {
            RotationUtils.silentSync();
        }
        toPlace.clear();

    }


    int getSlot()
    {
        int slot = -1;
        slot = InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN);
        if (slot == -1)
        {
            slot = InventoryUtils.getHotbarItemSlot(Items.ENDER_CHEST);
        }
        return slot;
    }


    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event)
    {
        if (NullUtils.nullCheck()) return;


        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "AutoFeetPlace"))
            return;


        if (breakCrystal.getValue() && predict.getValue() && event.getEntity() instanceof EndCrystalEntity entity && timer.isPassed() && hitcrystalCooldown.isPassed())
            doPredict(entity);

    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            final BlockPos targetPos = packet.getPos();
            if (placed.containsKey(targetPos))
            {
                placed.remove(targetPos);
            }
        }
    }

    public void doPredict(EndCrystalEntity entity)
    {
        int blockSlot = getSlot();

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;

        if (blockSlot == -1) return;

        for (BlockPos targetPos : ProtectionUtils.getSurroundPlacements(false, mode.getValue().equals("Dynamic"), adaptiveBox.getValue(), true, strictDirection.getValue()))
        {

            if (!BlockUtils.isReplaceable(targetPos))
                continue;


            if (!BlockUtils.isInterceptedBy(targetPos, entity)) continue;

            if (BlockUtils.isBlockedByIgnoreEntity(targetPos)) return;

            boolean rotated = false;

            if (CatAura.INSTANCE.rotate.getValue())
            {
                float[] rots = new float[]{RotationUtils.getActualYaw(), RotationUtils.getActualPitch()};
                if (RotationManager.INSTANCE.isRotating())
                {
                    if (!RotationManager.INSTANCE.requests.isEmpty())
                    {
                        Rotation rotation = RotationManager.INSTANCE.getRotationRequest();
                        rots = new float[]{rotation.getYaw(), rotation.getPitch()};
                    } else
                    {
                        rots = new float[]{RotationManager.INSTANCE.getRotation().getYaw(), RotationManager.INSTANCE.getRotation().getPitch()};
                    }
                }


                if (RaytraceUtils.isLookingResult(mc.player, entity, mc.player.getEyePos(), rots, 6.0f) == null)
                {
                    float[] breakRots = RotationUtils.getRotationsTo(mc.player.getEyePos(), new Vec3d(entity.getX(), entity.getY(), entity.getZ()));

                    if (CatAura.INSTANCE.rotate.getValue())
                    {
                        RotationUtils.doSilentRotate(breakRots);
                        rotated = true;
                    }
                }
            }


            PlayerUtils.attackTarget(entity);
            hitcrystalCooldown.resetDelay();

            if (blockSlot != mc.player.getInventory().selectedSlot)
            {
                InventoryUtils.switchToSlot(blockSlot);
                switched = true;
            }

            if (rotate.getValue() && AntiCheat.INSTANCE.protocol.getValue())
            {
                RotationUtils.doSilentRotate(targetPos, strictDirection.getValue());
                rotated = true;
            }

            BlockUtils.placeBlock(targetPos, BlockUtils.getPlaceableSide(targetPos, strictDirection.getValue()), !mc.player.getMainHandStack().getItem().equals(Items.ENDER_CHEST));


            if (render.getValue())
                renderPositions.put(targetPos, System.currentTimeMillis());


            if (switched)
                InventoryUtils.switchToSlot(oldSlot);

            if (rotated)
                RotationUtils.silentSync();
            return;
        }
    }


    public boolean canOverplace()
    {
        if (sequential.getValue()) return true;

        return breakCrystal.getValue() && hitcrystalCooldown.isPassed();
    }

    @Override
    public String getDescription()
    {
        return "AutoFeetPlace: Places blocks to protect you from malicious crystal attacks";
    }
}
