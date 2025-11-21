package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.impl.features.modules.player.Scaffold;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.skitttyy.kami.api.utils.render.RenderUtil.*;
import static me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers.LINE;

public class Trails extends Module
{

    Value<Number> lineWidth = new ValueBuilder<Number>()
            .withDescriptor("Line Width")
            .withValue(2)
            .withRange(0.1, 5)
            .register(this);
    Value<Number> lifetime = new ValueBuilder<Number>()
            .withDescriptor("Lifetime")
            .withValue(1000)
            .withRange(0, 5000)
            .register(this);

    Value<Boolean> fade = new ValueBuilder<Boolean>()
            .withDescriptor("Fade")
            .withValue(true)
            .register(this);

    Value<Boolean> xp = new ValueBuilder<Boolean>()
            .withDescriptor("XP")
            .withValue(false)
            .register(this);
    Value<Boolean> arrow = new ValueBuilder<Boolean>()
            .withDescriptor("Arrow")
            .withValue(false)
            .register(this);

    Value<Sn0wColor> startColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Start Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);
    Value<Sn0wColor> endColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("End Color")
            .withValue(new Sn0wColor(0, 255, 72))
            .register(this);
    Value<Boolean> self = new ValueBuilder<Boolean>()
            .withDescriptor("Self")
            .withValue(false)
            .register(this);
    Value<Number> selfTime = new ValueBuilder<Number>()
            .withDescriptor("Self Time")
            .withValue(1000)
            .withRange(0, 2000)
            .register(this);
/*
    Value<Boolean> target = new ValueBuilder<Boolean>()
            .withDescriptor("Target")
            .withValue(false)
            .register(this);
    Value<Number> targetTime = new ValueBuilder<Number>()
            .withDescriptor("Target Time")
            .withValue(1000)
            .withRange(0, 2000)
            .register(this);
*/

    public Trails()
    {
        super("Trails", Category.Render);
    }

    Map<Integer, ItemTrail> trails = new HashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.AfterClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;
        for (Entity entity : mc.world.getEntities())
        {

            if (allowEntity(entity))
            {
                if (trails.containsKey(entity.getId()))
                {
                    if (!entity.isAlive())
                    {

                        if (trails.get(entity.getId()).timer.isPaused())
                        {
                            trails.get(entity.getId()).timer.resetDelay();
                        }

                        trails.get(entity.getId()).timer.setPaused(false);
                    } else
                    {
                        trails.get(entity.getId()).positions.add(new Position(entity.getPos()));
                    }
                } else
                {
                    trails.put(entity.getId(), new ItemTrail(entity));

                }
            }
        }

        if (self.getValue())
        {
            if (trails.containsKey(mc.player.getId()))
            {
                ItemTrail playerTrail = trails.get(mc.player.getId());
                playerTrail.timer.resetDelay();
                List<Position> toRemove = new ArrayList<>();
                for (Position position : playerTrail.positions)
                {
                    if (System.currentTimeMillis() - position.time > selfTime.getValue().longValue())
                    {
                        toRemove.add(position);
                    }
                }
                playerTrail.positions.removeAll(toRemove);
                playerTrail.positions.add(new Position(mc.player.getPos()));
            } else
            {
                trails.put(mc.player.getId(), new ItemTrail(mc.player));
            }
        } else
        {
            if (trails.containsKey(mc.player.getId()))
            {
                trails.remove(mc.player.getId());
            }
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;


        for (Map.Entry<Integer, ItemTrail> entry : trails.entrySet())
        {
            if (!entry.getValue().entity.isAlive() || mc.world.getEntityById(entry.getKey()) == null)
            {
                if (entry.getValue().timer.isPaused())
                {
                    entry.getValue().timer.resetDelay();
                    entry.getValue().timer.setDelay(lifetime.getValue().longValue());
                }

                entry.getValue().timer.setPaused(false);

                if (entry.getValue().timer.isPassed(lifetime.getValue().longValue() + (fade.getValue() ? 500 : 0)))
                {
                    trails.remove(entry.getKey());
                    continue;
                }
            }

            drawTrail(entry.getValue());
        }
    }

    public void drawTrail(ItemTrail trail)
    {
        if (trail.positions.isEmpty()) return;


        int startAlpha = startColor.getValue().getAlpha();
        int endAlpha = endColor.getValue().getAlpha();

        if (fade.getValue() && !trail.timer.isPaused())
        {
            double normal = normalize(((double) (System.currentTimeMillis() - (trail.timer.getStartTime() + lifetime.getValue().doubleValue()))), 0d, 500);
            normal = MathHelper.clamp(normal, 0, 1);
            normal = -normal;
            normal++;

            startAlpha *= normal;
            endAlpha *= normal;
        }
        Color start = ColorUtil.newAlpha(startColor.getValue().getColor(), startAlpha);
        Color end = ColorUtil.newAlpha(endColor.getValue().getColor(), endAlpha);
        renderTrail(trail, start, end, trail.positions.get(0).pos);
    }


    public void renderTrail(ItemTrail trail, Color start, Color end, Vec3d first)
    {
        Vec3d lastPos = first;
        for (Position p : trail.positions)
        {
            double value = normalize(trail.positions.indexOf(p), 0, trail.positions.size());
            RenderUtil.renderLineFromPosToPos(lastPos, p.pos, ColorUtil.interpolate(((float) value), start, end), ColorUtil.interpolate(((float) value), start, end), lineWidth.getValue().floatValue());
            lastPos = p.pos;
        }
    }

    boolean allowEntity(Entity e)
    {

        if(!mc.world.getWorldBorder().contains(e.getPos())) return false;

        return e instanceof EnderPearlEntity || (e instanceof ExperienceBottleEntity && xp.getValue()) || (e instanceof ArrowEntity && arrow.getValue());
    }

    double normalize(double value, double min, double max)
    {
        return ((value - min) / (max - min));
    }

    public class ItemTrail
    {
        public Entity entity;
        public List<Position> positions;
        public Timer timer;

        public ItemTrail(Entity entity)
        {
            this.entity = entity;
            this.positions = new ArrayList<>();
            this.timer = new Timer();
            timer.setDelay(lifetime.getValue().longValue());
            this.timer.setPaused(true);
        }
    }

    public static class Position
    {
        public Vec3d pos;
        public long time;

        public Position(Vec3d pos)
        {
            this.pos = pos;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return time == position.time && Objects.equals(pos, position.pos);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(pos, time);
        }
    }

    @Override
    public String getDescription()
    {
        return "Trails: renders trails/breadcrumbs behind various things";
    }
}
