package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.player.PhaseWalk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class FastFall extends Module
{

    Value<Number> height = new ValueBuilder<Number>()
            .withDescriptor("Height")
            .withValue(2)
            .withRange(1, 10)
            .register(this);

    Value<Boolean> teleport = new ValueBuilder<Boolean>()
            .withDescriptor("Teleport")
            .withValue(false)
            .register(this);
    Value<Number> teleportHeight = new ValueBuilder<Number>()
            .withDescriptor("Tele-Height")
            .withValue(2)
            .withRange(1, 5)
            .withParent(teleport)
            .withParentEnabled(true)
            .register(this);

    public FastFall()
    {
        super("FastFall", Category.Movement);
    }

    private boolean prevOnGround = false;


    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isRiding()
                || mc.player.isFallFlying()
                || mc.player.isHoldingOntoLadder()
                || mc.player.isInLava()
                || mc.player.isTouchingWater()
                || mc.player.input.jumping
                || mc.player.input.sneaking)
        {
            return;
        }
        if (movementModuleCheck())
            return;
        if (mc.player.isOnGround() && isNearestBlockWithinHeight(height.getValue().floatValue()))
        {
            PlayerUtils.setMotionY(-3.0f);
        }
        if (teleport.getValue() && mc.player.fallDistance < 0.5D && prevOnGround && mc.player.isOnGround())
        {
            double downTrace = getGroundLevel();
            if (mc.player.getY() - downTrace <= teleportHeight.getValue().floatValue())
            {
                Box mybb = mc.player.getBoundingBox();
                if (!isColliding(new Box(mybb.minX, downTrace, mybb.minZ, mybb.maxX, downTrace + mc.player.getHeight(), mybb.maxZ)) && !(downTrace > mc.player.getY()))
                {

                    mc.player.setPosition(mc.player.getX(), downTrace, mc.player.getZ());
                }
            }
        }
        prevOnGround = mc.player.isOnGround();
    }

    private boolean isNearestBlockWithinHeight(double height)
    {
        Box bb = mc.player.getBoundingBox();
        for (double i = 0; i < height + 0.5; i += 0.01)
        {
            if (!mc.world.isSpaceEmpty(mc.player, bb.offset(0, -i, 0)))
            {
                return true;
            }
        }
        return false;
    }

    public boolean movementModuleCheck()
    {
        return Speed.INSTANCE.isEnabled() || Holesnap.INSTANCE.isEnabled() || PhaseWalk.INSTANCE.isEnabled() || Flight.INSTANCE.isEnabled() || LongJump.INSTANCE.isEnabled();
    }

    public static double getGroundLevel()
    {
        for (int i = (int) Math.round(mc.player.getY()); i > mc.world.getBottomY(); --i)
        {
            Box mybb = mc.player.getBoundingBox();
            Box box = new Box(mybb.minX, i - 1, mybb.minZ, mybb.maxX, i, mybb.maxZ);
            if (!isColliding(box) || !(box.minY <= mc.player.getY()))
            {
                continue;
            }
            return i;
        }
        return mc.player.getY();
    }

    public static boolean isColliding(final Box box)
    {
        return mc.world.getBlockCollisions(mc.player, box).iterator().hasNext();
    }

    @Override
    public String getDescription()
    {
        return "FastFall/ReverseStep: \"Falls Down\" faster";
    }
}

