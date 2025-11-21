package me.skitttyy.kami.impl.features.modules.movement;

import com.google.common.collect.Streams;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.List;

public class Parkour extends Module {

    public static Parkour INSTANCE;

    public Parkour()
    {
        super("Parkour", Category.Movement);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (!mc.player.isOnGround() || mc.options.jumpKey.isPressed()) return;

        if (mc.player.isSneaking() || mc.options.sneakKey.isPressed()) return;



        Box entityBoundingBox = mc.player.getBoundingBox();
        Box offsetBox = entityBoundingBox.offset(0, -0.5, 0).expand(-0.001, 0, -0.001);

        Iterable<VoxelShape> collisionBoxes = mc.world.getCollisions(mc.player, offsetBox);

        if (!Streams.stream(collisionBoxes).toList().isEmpty()) return;

        mc.player.jump();
    }


    @Override
    public String getDescription()
    {
        return "Parkour: Jumps at edge of blocks";
    }
}
