package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.world.CollisionBoxEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShapes;

public class Heaven extends Module {


    public Heaven()
    {
        super("Heaven", Category.Misc);
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isDead())
            PlayerUtils.setMotionY(0.4);
    }

    @SubscribeEvent
    public void onCollision(CollisionBoxEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mc.player.isDead())
        {
            event.setCancelled(true);
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }
    }

    @Override
    public String getDescription()
    {
        return "Heaven: get sent to heaven after u die";
    }

}
