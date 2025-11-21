package me.skitttyy.kami.api.management;

import lombok.Getter;
import me.skitttyy.kami.api.event.eventbus.Priority;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.key.InputEvent;
import me.skitttyy.kami.api.event.events.move.LookEvent;
import me.skitttyy.kami.api.event.events.move.MovementPacketsEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.utils.ducks.IClientPlayerEntity;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.movement.Disabler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Integer.MAX_VALUE;


public class RotationManager implements IMinecraft
{
    public static RotationManager INSTANCE;
    public final List<Rotation> requests = new ArrayList<>();
    // Relevant rotation values
    @Getter
    private float serverYaw, serverPitch, lastServerYaw, lastServerPitch, prevJumpYaw, prevYaw, prevPitch;
    boolean rotate;

    public int serverSlot = -1;

    public boolean FROM_INV = false;
    // The current in use rotation

    public static final List<InventoryUtils.PreSwapData> swapData = new CopyOnWriteArrayList<>();


    @Getter
    private Rotation rotation;
    private int rotateTicks;

    public RotationManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent(Priority.MANAGER_FIRST)
    public void onPacketOutbound(PacketEvent.Send event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook())
        {
            float packetYaw = packet.getYaw(0.0f);
            float packetPitch = packet.getPitch(0.0f);
            serverYaw = packetYaw;
            serverPitch = packetPitch;
        }

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            if (serverSlot == packet.getSelectedSlot()) event.setCancelled(true);


            serverSlot = packet.getSelectedSlot();
        }
    }

    private static final List<Runnable> afterTick = new ArrayList<>();

    public static void scheduleNextTick(Runnable callback)
    {
        afterTick.add(callback);
    }

    @SubscribeEvent
    public void playerTickEvent(TickEvent.PlayerTickEvent.Post event)
    {
        for (Runnable callback : afterTick)
        {
            callback.run();
        }
        afterTick.clear();
    }

    public void onUpdate()
    {
        if (requests.isEmpty())
        {
            rotation = null;
            return;
        }
        Rotation request = getRotationRequest();
        if (request == null)
        {
            rotation = null;
            return;
        } else
        {
            rotation = request;
        }
        // fixes flags for aim % 360
        // GCD implementation maybe?
        if (rotation == null)
        {
            return;
        }
//        rotateTicks = 0;
        rotate = true;
    }

    @SubscribeEvent(Priority.MANAGER_FIRST)
    public void onMovementPackets(MovementPacketsEvent event)
    {
        if (rotation != null)
        {

            if (rotate)
            {
                removeRotation(rotation);
                event.setCancelled(true);
                event.setYaw(rotation.getYaw());
                event.setPitch(rotation.getPitch());
                rotate = false;
            }

            if (rotation.isSnap())
            {
                rotation = null;
            }
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        swapData.removeIf(InventoryUtils.PreSwapData::isPassedClearTime);
    }

    @SubscribeEvent
    public void onMovementTick(final TickEvent.MovementTickEvent.Post event)
    {
        lastServerYaw = ((IClientPlayerEntity) mc.player).getLastSpoofedYaw();
        lastServerPitch = ((IClientPlayerEntity) mc.player).getLastSpoofedPitch();
    }

    @SubscribeEvent
    public void onKeyboardTick(InputEvent event)
    {
        if (rotation != null && mc.player != null && AntiCheat.INSTANCE.strafeFix.getValue() && !(Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.mode.getValue().equals("Overflow")))
        {
            if (KamiMod.isBaritonePaused())
                return;
            float forward = mc.player.input.movementForward;
            float sideways = mc.player.input.movementSideways;
            float delta = (mc.player.getYaw() - rotation.getYaw()) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            mc.player.input.movementSideways = Math.round(sideways * cos - forward * sin);
            mc.player.input.movementForward = Math.round(forward * cos + sideways * sin);
        }
    }

    @SubscribeEvent
    public void onUpdateVelocity(LookEvent.LookVelocityEvent event)
    {

        if (KamiMod.isBaritonePaused())
            return;

        if (rotation != null && mc.player != null && AntiCheat.INSTANCE.strafeFix.getValue() && !(Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.mode.getValue().equals("Overflow")))
        {
            event.setVelocity(movementInputToVelocity(rotation.getYaw(), event.getMovementInput(), event.getSpeed()));
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.Jump event)
    {

        if (KamiMod.isBaritonePaused())
            return;

        if (rotation == null || !AntiCheat.INSTANCE.strafeFix.getValue()) return;

        event.setYaw(rotation.getYaw());
    }

    public final Vec3d getRotationVector()
    {
        if (rotation == null) return null;

        float f = rotation.getPitch() * 0.017453292F;
        float g = -rotation.getYaw() * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double) (i * j), (double) (-k), (double) (h * j));
    }

    /**
     * @param in
     */
    public void rotateTo(Rotation in)
    {


        Rotation rotation = in.fix();
        if (rotation.getPriority() == MAX_VALUE)
        {
            this.rotation = rotation;
        }

        Rotation request = requests.stream().filter(r -> rotation.getPriority() == r.getPriority()).findFirst().orElse(null);
        if (request == null)
        {
            requests.add(rotation);
        } else
        {
            // r.setPriority();
            request.setYaw(rotation.getYaw());
            request.setPitch(rotation.getPitch());
        }
    }

    public void setRotationClient(float yaw, float pitch)
    {
        if (mc.player == null)
        {
            return;
        }
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }


    /**
     * @param request
     */
    public boolean removeRotation(Rotation request)
    {
        return requests.remove(request);
    }

    public boolean isRotationBlocked(int priority)
    {
        return rotation != null && priority < rotation.getPriority();
    }

//    public boolean isDoneRotating()
//    {
//        return rotateTicks > Rotations.INSTANCE.preserveTicks.getValue().intValue();
//    }

    public boolean isRotating()
    {
        return rotation != null || !requests.isEmpty();
    }

    public float getRotationYaw()
    {
        if (this.rotation == null) return mc.player.getYaw();

        return rotation.getYaw();
    }

    public float getRotationPitch()
    {

        if (this.rotation == null) return mc.player.getPitch();


        return rotation.getPitch();
    }

    public float getWrappedYaw()
    {
        return MathHelper.wrapDegrees(serverYaw);
    }


    private Vec3d movementInputToVelocity(float yaw, Vec3d movementInput, float speed)
    {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7)
        {
            return Vec3d.ZERO;
        }
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }

    public Rotation getRotationRequest()
    {
        Rotation rotationRequest = null;
        int priority = 0;
        for (Rotation request : requests)
        {
            if (request.getPriority() > priority)
            {
                rotationRequest = request;
                priority = request.getPriority();
            }
        }
        return rotationRequest;
    }
}