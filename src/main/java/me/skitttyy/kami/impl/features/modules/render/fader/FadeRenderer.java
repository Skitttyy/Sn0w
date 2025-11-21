package me.skitttyy.kami.impl.features.modules.render.fader;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.combat.*;
import me.skitttyy.kami.impl.features.modules.player.Scaffold;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FadeRenderer {
    public static FadeRenderer INSTANCE;

    public FadeRenderer()
    {
        KamiMod.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {

        doCrystalRender();
        doAnchorRender();

        doAutoFeetPlaceRender();
        doSelfTrapRender();
        doAutoPlaceRender();

        doScaffoldRender();
    }

    public void doCrystalRender()
    {

        if (!CatAura.INSTANCE.renderMode.getValue().equals("Fade")) return;


        Color fillColor = CatAura.INSTANCE.fillColorS.getValue().getColor();
        Color lineColor = CatAura.INSTANCE.lineColorS.getValue().getColor();
        BlockPos renderPos = CatAura.INSTANCE.renderPos;

        if (!CatAura.oldPlacements.isEmpty())
            CatAura.oldPlacements = CatAura.oldPlacements.stream().filter(Objects::nonNull).distinct().filter((crystal) -> System.currentTimeMillis() - crystal.value() < CatAura.INSTANCE.fadeTime.getValue().longValue()).collect(Collectors.toList());
        if (renderPos != null && CatAura.INSTANCE.canRender() && CatAura.INSTANCE.target != null)
        {
            RenderUtil.renderBox(
                    RenderType.FILL,
                    new Box(renderPos),
                    fillColor,
                    fillColor
            );
            RenderUtil.renderBox(
                    RenderType.LINES,
                    new Box(renderPos),
                    lineColor,
                    lineColor
            );

            if (CatAura.INSTANCE.renderDamage.getValue())
                RenderBuffers.schedulePreRender(() ->
                {
                    RenderUtil.drawText(String.format("%.1f", CatAura.INSTANCE.renderDMG), new Box(renderPos).getCenter(), CatAura.INSTANCE.textScale.getValue().floatValue());
                });


        }
        if (CatAura.INSTANCE.futureFade.getValue() && renderPos != null)
        {
            CatAura.oldPlacements.removeIf(pair -> !pair.key().equals(renderPos));
        }
        for (Pair.BlockPairTime pair : CatAura.oldPlacements)
        {
            if (renderPos == null || !pair.key().equals(renderPos))
            {
                double normal = MathUtil.normalize((double) (System.currentTimeMillis() - pair.value()), 0.0D, CatAura.INSTANCE.fadeTime.getValue().doubleValue());
                Color fillFade = ColorUtil.interpolate((float) normal, ColorUtil.newAlpha(fillColor, 0), fillColor);
                Color outlineFade = ColorUtil.interpolate((float) normal, ColorUtil.newAlpha(lineColor, 0), lineColor);

                RenderUtil.renderBox(
                        RenderType.FILL,
                        new Box(pair.key()),
                        fillFade,
                        fillFade
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        new Box(pair.key()),
                        outlineFade,
                        outlineFade
                );
            }
        }
    }

    public void doAnchorRender()
    {

        if (!AutoAnchor.INSTANCE.renderMode.getValue().equals("Fade")) return;


        Color fillColor = AutoAnchor.INSTANCE.fillColorS.getValue().getColor();
        Color lineColor = AutoAnchor.INSTANCE.lineColorS.getValue().getColor();
        BlockPos renderPos = AutoAnchor.INSTANCE.renderPos;

        if (!AutoAnchor.oldPlacements.isEmpty())
            AutoAnchor.oldPlacements = AutoAnchor.oldPlacements.stream().filter(Objects::nonNull).distinct().filter((crystal) -> System.currentTimeMillis() - crystal.value() < AutoAnchor.INSTANCE.fadeTime.getValue().longValue()).collect(Collectors.toList());
        if (renderPos != null && AutoAnchor.INSTANCE.target != null)
        {
            RenderUtil.renderBox(
                    RenderType.FILL,
                    new Box(renderPos),
                    fillColor,
                    fillColor
            );
            RenderUtil.renderBox(
                    RenderType.LINES,
                    new Box(renderPos),
                    lineColor,
                    lineColor
            );

            if (AutoAnchor.INSTANCE.renderDamage.getValue())
                RenderBuffers.schedulePreRender(() ->
                {
                    RenderUtil.drawText(String.format("%.1f", AutoAnchor.INSTANCE.renderDMG), new Box(renderPos).getCenter(), AutoAnchor.INSTANCE.textScale.getValue().floatValue());
                });


        }
        if (AutoAnchor.INSTANCE.futureFade.getValue() && renderPos != null)
        {
            AutoAnchor.oldPlacements.removeIf(pair -> !pair.key().equals(renderPos));
        }
        for (Pair.BlockPairTime pair : AutoAnchor.oldPlacements)
        {
            if (renderPos == null || !pair.key().equals(renderPos))
            {
                double normal = MathUtil.normalize((double) (System.currentTimeMillis() - pair.value()), 0.0D, AutoAnchor.INSTANCE.fadeTime.getValue().doubleValue());
                Color fillFade = ColorUtil.interpolate((float) normal, ColorUtil.newAlpha(fillColor, 0), fillColor);
                Color outlineFade = ColorUtil.interpolate((float) normal, ColorUtil.newAlpha(lineColor, 0), lineColor);

                RenderUtil.renderBox(
                        RenderType.FILL,
                        new Box(pair.key()),
                        fillFade,
                        fillFade
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        new Box(pair.key()),
                        outlineFade,
                        outlineFade
                );
            }
        }
    }
    public void doAutoFeetPlaceRender()
    {
        if (!AutoFeetPlace.INSTANCE.render.getValue()) return;


        for (Map.Entry<BlockPos, Long> entry : AutoFeetPlace.INSTANCE.renderPositions.entrySet())
        {
            int fillAlpha = AutoFeetPlace.INSTANCE.fill.getValue().getAlpha();
            int lineAlpha = AutoFeetPlace.INSTANCE.line.getValue().getAlpha();

            long time = System.currentTimeMillis() - entry.getValue();
            double normal = MathUtil.normalize(time, 0, AutoFeetPlace.INSTANCE.fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            fillAlpha *= normal;
            lineAlpha *= normal;

            Color fillColor = ColorUtil.newAlpha(AutoFeetPlace.INSTANCE.fill.getValue().getColor(), fillAlpha);
            Color lineColor = ColorUtil.newAlpha(AutoFeetPlace.INSTANCE.line.getValue().getColor(), lineAlpha);

            Box bb = new Box(entry.getKey());

            RenderUtil.renderBox(RenderType.FILL, bb, fillColor, fillColor);
            RenderUtil.renderBox(RenderType.LINES, bb, lineColor, lineColor);
            if (fillAlpha == 0 && lineAlpha == 0)
            {
                AutoFeetPlace.INSTANCE.renderPositions.remove(entry.getKey());
            }
        }
    }

    public void doAutoPlaceRender()
    {
        if (!AutoPlacer.INSTANCE.render.getValue()) return;


        for (Map.Entry<BlockPos, Long> entry : AutoPlacer.INSTANCE.renderPositions.entrySet())
        {
            int fillAlpha = AutoPlacer.INSTANCE.fill.getValue().getAlpha();
            int lineAlpha = AutoPlacer.INSTANCE.line.getValue().getAlpha();

            long time = System.currentTimeMillis() - entry.getValue();
            double normal = MathUtil.normalize(time, 0, AutoPlacer.INSTANCE.fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            fillAlpha *= normal;
            lineAlpha *= normal;

            Color fillColor = ColorUtil.newAlpha(AutoPlacer.INSTANCE.fill.getValue().getColor(), fillAlpha);
            Color lineColor = ColorUtil.newAlpha(AutoPlacer.INSTANCE.line.getValue().getColor(), lineAlpha);

            Box bb = new Box(entry.getKey());

            RenderUtil.renderBox(RenderType.FILL, bb, fillColor, fillColor);
            RenderUtil.renderBox(RenderType.LINES, bb, lineColor, lineColor);
            if (fillAlpha == 0 && lineAlpha == 0)
            {
                AutoPlacer.INSTANCE.renderPositions.remove(entry.getKey());
            }
        }
    }

    
    public void doSelfTrapRender()
    {
        if (!SelfTrap.INSTANCE.render.getValue()) return;


        for (Map.Entry<BlockPos, Long> entry : SelfTrap.INSTANCE.renderPositions.entrySet())
        {
            int fillAlpha = SelfTrap.INSTANCE.fill.getValue().getAlpha();
            int lineAlpha = SelfTrap.INSTANCE.line.getValue().getAlpha();

            long time = System.currentTimeMillis() - entry.getValue();
            double normal = MathUtil.normalize(time, 0, SelfTrap.INSTANCE.fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            fillAlpha *= normal;
            lineAlpha *= normal;

            Color fillColor = ColorUtil.newAlpha(SelfTrap.INSTANCE.fill.getValue().getColor(), fillAlpha);
            Color lineColor = ColorUtil.newAlpha(SelfTrap.INSTANCE.line.getValue().getColor(), lineAlpha);

            Box bb = new Box(entry.getKey());

            RenderUtil.renderBox(RenderType.FILL, bb, fillColor, fillColor);
            RenderUtil.renderBox(RenderType.LINES, bb, lineColor, lineColor);
            if (fillAlpha == 0 && lineAlpha == 0)
            {
                SelfTrap.INSTANCE.renderPositions.remove(entry.getKey());
            }
        }
    }


    public void doScaffoldRender()
    {
        if (!Scaffold.INSTANCE.render.getValue()) return;


        for (Map.Entry<BlockPos, Long> entry : Scaffold.INSTANCE.renderPositions.entrySet())
        {
            int fillAlpha = Scaffold.INSTANCE.fill.getValue().getAlpha();
            int lineAlpha = Scaffold.INSTANCE.line.getValue().getAlpha();

            long time = System.currentTimeMillis() - entry.getValue();
            double normal = MathUtil.normalize(time, 0, Scaffold.INSTANCE.fadeTime.getValue().doubleValue());
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            fillAlpha *= normal;
            lineAlpha *= normal;

            Color fillColor = ColorUtil.newAlpha(Scaffold.INSTANCE.fill.getValue().getColor(), fillAlpha);
            Color lineColor = ColorUtil.newAlpha(Scaffold.INSTANCE.line.getValue().getColor(), lineAlpha);

            Box bb = new Box(entry.getKey());

            RenderUtil.renderBox(RenderType.FILL, bb, fillColor, fillColor);
            RenderUtil.renderBox(RenderType.LINES, bb, lineColor, lineColor);
            if (fillAlpha == 0 && lineAlpha == 0)
            {
                Scaffold.INSTANCE.renderPositions.remove(entry.getKey());
            }
        }
    }
    
}
