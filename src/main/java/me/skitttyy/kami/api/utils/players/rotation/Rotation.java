package me.skitttyy.kami.api.utils.players.rotation;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

@Getter
@Setter
public class Rotation implements IMinecraft
{
    private final int priority;
    private float yaw, pitch;
    private boolean snap;

    public Rotation(int priority, float yaw, float pitch, boolean snap)
    {
        this.priority = priority;
        this.yaw = yaw;
        this.pitch = pitch;
        this.snap = snap;
    }

    public Rotation(int priority, float yaw, float pitch)
    {
        this.priority = priority;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(float yaw, float pitch)
    {
        this.priority = 100;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation correctSensitivity(Rotation prevRotation)
    {

        Rotation deltaRotation = closestDelta(prevRotation);
        Pair<Double, Double>[] cursorDeltas = approximateCursorDeltas(deltaRotation);
        Rotation[] newRotations = Arrays.stream(cursorDeltas)
                .map(cursorDelta -> calculateNewRotation(prevRotation, cursorDelta))
                .toArray(Rotation[]::new);

        return Arrays.stream(newRotations).min((r1, r2) -> Float.compare(fov(r1), fov(r2))).orElse(newRotations[0]);
    }

    public Rotation calculateNewRotation(Rotation prevRotation, Pair<Double, Double> cursorDeltas)
    {
        double gcd = getGcd();
        Rotation rotationChange = new Rotation((float) (cursorDeltas.key() * gcd) * 0.15F, (float) (cursorDeltas.value() * gcd) * 0.15F);
        Rotation newRotation = new Rotation(prevRotation.yaw + rotationChange.yaw, prevRotation.pitch + rotationChange.pitch);
        newRotation = new Rotation(this.priority, newRotation.yaw, Math.max(-90F, Math.min(90F, newRotation.pitch)));
        return newRotation;
    }

    private float dist(Rotation deltaRotation)
    {
        return (float) Math.sqrt(deltaRotation.yaw * deltaRotation.yaw + deltaRotation.pitch * deltaRotation.pitch);
    }

    public float fov(Rotation other)
    {
        return dist(closestDelta(other));
    }

    private static double getGcd()
    {
        double sensitivity = mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
        double sensitivityPow3 = sensitivity * sensitivity * sensitivity;
        double sensitivityPow3Mult8 = sensitivityPow3 * 8.0;

        return (mc.options.getPerspective().isFirstPerson() && mc.player != null && mc.player.isUsingSpyglass())
                ? sensitivityPow3
                : sensitivityPow3Mult8;
    }

    public static Pair<Double, Double>[] approximateCursorDeltas(Rotation deltaRotation)
    {
        double gcd = getGcd() * 0.15F;
        double targetX = -deltaRotation.yaw / gcd;
        double targetY = -deltaRotation.pitch / gcd;
        return new Pair[]{
                new Pair<>(Math.floor(targetX), Math.floor(targetY)),
                new Pair<>(Math.ceil(targetX), Math.floor(targetY)),
                new Pair<>(Math.ceil(targetX), Math.ceil(targetY)),
                new Pair<>(Math.floor(targetX), Math.ceil(targetY))
        };
    }

    public Rotation closestDelta(Rotation other)
    {
        return new Rotation(MathHelper.wrapDegrees(other.yaw - yaw), other.pitch - pitch);
    }

    public Rotation fix()
    {

        Rotation rotation = RotationManager.INSTANCE.getRotation();
        if (rotation == null)
            rotation = new Rotation(this.priority, RotationManager.INSTANCE.getRotationYaw(), RotationManager.INSTANCE.getRotationPitch());
        return correctSensitivity(rotation);
    }

}