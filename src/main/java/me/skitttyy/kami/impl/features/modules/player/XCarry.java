package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.RaytraceUtils;
import me.skitttyy.kami.impl.features.modules.combat.Auto32k;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;

public class XCarry extends Module {
    public static XCarry INSTANCE;

    public XCarry()
    {
        super("XCarry", Category.Player);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket)
        {
            event.setCancelled(true);
        }
    }


    @Override
    public String getDescription()
    {
        return "XCarry: carry item(s) in ur holding slots like meowlauncer";
    }
}
