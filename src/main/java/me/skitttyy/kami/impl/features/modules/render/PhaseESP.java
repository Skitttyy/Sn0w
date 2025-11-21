package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class PhaseESP extends Module
{
    public static PhaseESP INSTANCE;

    public PhaseESP()
    {
        super("PhaseESP", Category.Render);
        INSTANCE = this;
    }
    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(0, 255, 218, 100))
            .register(this);
    Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<Number> height = new ValueBuilder<Number>()
            .withDescriptor("Height")
            .withValue(1)
            .withRange(0, 2)
            .register(this);


    public List<BlockPos> phases = new ArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        phases.clear();
        phases.addAll(getPhaseBlocks());

    }

    public List<BlockPos> getPhaseBlocks()
    {
        List<BlockPos> blocks = new ArrayList<>();

        Box bb = mc.player.getBoundingBox();
        for (BlockPos pos : BlockUtils.getAllInBox(bb.shrink(0, 1.2, 0).expand(0.1, 0, 0.1)))
        {
            if (pos.equals(mc.player.getBlockPos())) continue;

            if (mc.world.getBlockState(pos).blocksMovement())
            {
                if (!mc.world.getBlockState(pos.down()).blocksMovement() && mc.world.isInBuildLimit(pos.down()))
                {
                    blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (phases.isEmpty()) return;


        for (BlockPos pos : phases)
        {
            Box holeBB = new Box(pos);

            holeBB = new Box(holeBB.minX, holeBB.minY, holeBB.minZ, holeBB.maxX, holeBB.minY + height.getValue().doubleValue(), holeBB.maxZ);
            RenderUtil.renderBox(
                    RenderType.FILL,
                    holeBB,
                    fill.getValue().getColor(),
                    fill.getValue().getColor()
            );
            RenderUtil.renderBox(
                    RenderType.LINES,
                    holeBB,
                    line.getValue().getColor(),
                    line.getValue().getColor()
            );

        }
    }

    @Override
    public String getDescription()
    {
        return "PhaseESP: Highlight block ur colliding with if have air under them";
    }
}
