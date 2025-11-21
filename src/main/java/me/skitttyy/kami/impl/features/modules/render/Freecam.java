package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.key.KeyboardEvent;
import me.skitttyy.kami.api.event.events.key.MouseEvent;
import me.skitttyy.kami.api.event.events.render.ScreenEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;


public class Freecam extends Module
{

    public static Freecam INSTANCE;

    public Freecam()
    {
        super("Freecam", Category.Render);
        INSTANCE = this;

    }

    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(1D)
            .withRange(0.1D, 4.0D)
            .withPlaces(2)
            .register(this);
    Value<Number> vertical = new ValueBuilder<Number>()
            .withDescriptor("Vertical Speed")
            .withValue(0.5D)
            .withRange(0.1D, 2.0D)
            .withPlaces(2)
            .register(this);
    public Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);

    public final Vector3d pos = new Vector3d();
    public final Vector3d prevPos = new Vector3d();

    private Perspective perspective;

    public float yaw, pitch;
    public float prevYaw, prevPitch;

    private double fovScale;
    private boolean bobView;

    private boolean forward, backward, right, left, up, down;


    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player == null || mc.world == null)
        {
            toggle();
            return;
        }


        fovScale = mc.options.getFovEffectScale().getValue().floatValue();
        bobView = mc.options.getBobView().getValue();
        mc.options.getFovEffectScale().setValue((double) 0);
        mc.options.getBobView().setValue(false);
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();


        RenderUtil.set(pos, mc.gameRenderer.getCamera().getPos());
        RenderUtil.set(prevPos, mc.gameRenderer.getCamera().getPos());

        prevYaw = yaw;
        prevPitch = pitch;

        forward = mc.options.forwardKey.isPressed();
        backward = mc.options.backKey.isPressed();
        right = mc.options.rightKey.isPressed();
        left = mc.options.leftKey.isPressed();
        up = mc.options.jumpKey.isPressed();
        down = mc.options.sneakKey.isPressed();

        unpress();
    }

    @SubscribeEvent
    private void onOpenScreen(ScreenEvent.SetScreen event)
    {

        if (!checkGuiMove())
            unpress();

        prevPos.set(pos);
        prevYaw = yaw;
        prevPitch = pitch;
    }


    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;


        mc.options.setPerspective(perspective);
        mc.options.getFovEffectScale().setValue((double) fovScale);
        mc.options.getBobView().setValue(bobView);
        mc.options.forwardKey.setPressed(forward);
        mc.options.backKey.setPressed(backward);
        mc.options.rightKey.setPressed(right);
        mc.options.leftKey.setPressed(left);
        mc.options.jumpKey.setPressed(up);
        mc.options.sneakKey.setPressed(down);
    }

    private void unpress()
    {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (rotate.getValue() && mc.crosshairTarget != null && mc.crosshairTarget.getPos() != null && (mc.options.useKey.isPressed() || mc.options.attackKey.isPressed())) {
            RotationUtils.setRotation(RotationUtils.getRotationsTo(mc.player.getEyePos(), mc.crosshairTarget.getPos()));

        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.AfterClientTickEvent event)
    {
        if (mc.cameraEntity.isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velY = 0;
        double velZ = 0;


        double s = 0.5;
        if (mc.options.sprintKey.isPressed()) s = 1;

        boolean a = false;
        if (this.forward)
        {
            velX += forward.x * s * speed.getValue().floatValue();
            velZ += forward.z * s * speed.getValue().floatValue();
            a = true;
        }
        if (this.backward)
        {
            velX -= forward.x * s * speed.getValue().floatValue();
            velZ -= forward.z * s * speed.getValue().floatValue();
            a = true;
        }

        boolean b = false;
        if (this.right)
        {
            velX += right.x * s * speed.getValue().floatValue();
            velZ += right.z * s * speed.getValue().floatValue();
            b = true;
        }
        if (this.left)
        {
            velX -= right.x * s * speed.getValue().floatValue();
            velZ -= right.z * s * speed.getValue().floatValue();
            b = true;
        }

        if (a && b)
        {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }

        if (this.up)
        {
            velY += s * vertical.getValue().floatValue();
        }
        if (this.down)
        {
            velY -= s * vertical.getValue().floatValue();
        }

        prevPos.set(pos);
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ);
    }

    @SubscribeEvent
    public void onKey(KeyboardEvent event)
    {
        if (checkGuiMove()) return;

        boolean cancel = true;

        if (mc.options.forwardKey.matchesKey(event.getKey(), 0))
        {
            forward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.forwardKey.setPressed(false);
        } else if (mc.options.backKey.matchesKey(event.getKey(), 0))
        {
            backward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.backKey.setPressed(false);
        } else if (mc.options.rightKey.matchesKey(event.getKey(), 0))
        {
            right = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.rightKey.setPressed(false);
        } else if (mc.options.leftKey.matchesKey(event.getKey(), 0))
        {
            left = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.leftKey.setPressed(false);
        } else if (mc.options.jumpKey.matchesKey(event.getKey(), 0))
        {
            up = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.jumpKey.setPressed(false);
        } else if (mc.options.sneakKey.matchesKey(event.getKey(), 0))
        {
            down = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.sneakKey.setPressed(false);
        } else
        {
            cancel = false;
        }

        if (cancel) event.setCancelled(true);
    }

    @SubscribeEvent
    private void onMouseButton(MouseEvent event)
    {
        if (checkGuiMove()) return;

        boolean cancel = true;

        if (mc.options.forwardKey.matchesMouse(event.getButton()))
        {
            forward = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.forwardKey.setPressed(false);
        } else if (mc.options.backKey.matchesMouse(event.getButton()))
        {
            backward = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.backKey.setPressed(false);
        } else if (mc.options.rightKey.matchesMouse(event.getButton()))
        {
            right = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.rightKey.setPressed(false);
        } else if (mc.options.leftKey.matchesMouse(event.getButton()))
        {
            left = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.leftKey.setPressed(false);
        } else if (mc.options.jumpKey.matchesMouse(event.getButton()))
        {
            up = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.jumpKey.setPressed(false);
        } else if (mc.options.sneakKey.matchesMouse(event.getButton()))
        {
            down = !event.getType().equals(MouseEvent.Type.LIFT);
            mc.options.sneakKey.setPressed(false);
        } else
        {
            cancel = false;
        }

        if (cancel) event.setCancelled(true);
    }

    public void changeLookDirection(double deltaX, double deltaY)
    {
        prevYaw = yaw;
        prevPitch = pitch;

        yaw += deltaX;
        pitch += deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }


    public double getX(float tickDelta)
    {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }

    public double getY(float tickDelta)
    {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }

    public double getZ(float tickDelta)
    {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public double getYaw(float tickDelta)
    {
        return MathHelper.lerp(tickDelta, prevYaw, yaw);
    }

    public double getPitch(float tickDelta)
    {
        return MathHelper.lerp(tickDelta, prevPitch, pitch);
    }

    private boolean checkGuiMove()
    {
        // TODO: This is very bad but you all can cope :cope:

        if (mc.currentScreen != null && !(NoSlow.INSTANCE.isEnabled() && NoSlow.INSTANCE.guiMove.getValue()))
            return true;

        return false;
    }

    @Override
    public String getDescription()
    {
        return "Freecam: lets you move your camera around to get a better perspective";
    }
}
