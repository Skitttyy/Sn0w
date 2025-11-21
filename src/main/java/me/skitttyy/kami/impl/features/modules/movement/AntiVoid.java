package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class AntiVoid extends Module {

    public AntiVoid()
    {
        super("AntiVoid", Category.Movement);
    }

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Fling")
            .withModes("Fling", "Float", "Bounce", "Packet")
            .register(this);


    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        BlockPos pos = mc.player.getBlockPos();
        BlockPos pos2 = new BlockPos(mc.player.getBlockX(), mc.world.getBottomY(), mc.player.getBlockZ());
        if (mc.world.getBlockState(pos2).getBlock() == Blocks.AIR)
        {
            if (pos.equals(pos2))
            {
                switch (mode.getValue())
                {
                    case "Fling":
                        PlayerUtils.setMotionY(1);
                        break;
                    case "Bounce":
                        mc.player.jump();
                        break;
                    case "Float":
                        PlayerUtils.setMotionY(0);
                        break;
                    case "Packet":
                        PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 3, mc.player.getZ(), false));
                        break;
                }
            }
        }
    }


    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "AntiVoid: Prevents you from falling in the deep dark and scary void";
    }

}
