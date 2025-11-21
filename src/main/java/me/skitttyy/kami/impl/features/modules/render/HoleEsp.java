package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HoleEsp extends Module {
    Value<String> glowMode = new ValueBuilder<String>()
            .withDescriptor("Glow Mode")
            .withValue("Fade")
            .withModes("Fade", "None")
            .register(this);
    Value<String> animation = new ValueBuilder<String>()
            .withDescriptor("Animation")
            .withValue("None")
            .withModes("None", "Fade", "Height")
            .register(this);
    Value<Number> height = new ValueBuilder<Number>()
            .withDescriptor("Height")
            .withValue(1)
            .withRange(0, 2)
            .register(this);
    Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(5d)
            .withRange(1d, 30d)
            .register(this);
    Value<Boolean> doubles = new ValueBuilder<Boolean>()
            .withDescriptor("Doubles")
            .withValue(true)
            .register(this);
    Value<Sn0wColor> bedrockFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Bedrock Fill")
            .withValue(new Sn0wColor(0, 255, 0, 100))
            .register(this);
    Value<Sn0wColor> bedrockLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Bedrock Line")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<Sn0wColor> obbyFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Obby Fill")
            .withValue(new Sn0wColor(0, 255, 218, 100))
            .register(this);
    Value<Sn0wColor> obbyLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Obby Line")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<Sn0wColor> doubleFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Double Fill")
            .withValue(new Sn0wColor(255, 0, 11, 100))
            .register(this);
    Value<Sn0wColor> doubleLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Double Line")
            .withValue(new Sn0wColor(255, 255, 255, 255))
            .register(this);
    Value<Sn0wColor> bedrockFill2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Bedrock Fill2")
            .withValue(new Sn0wColor(0, 255, 0, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Sn0wColor> bedrockLine2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Bedrock Line2")
            .withValue(new Sn0wColor(255, 255, 255, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Sn0wColor> obbyFill2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Obby Fill2")
            .withValue(new Sn0wColor(0, 255, 218, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Sn0wColor> obbyLine2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Obby Line2")
            .withValue(new Sn0wColor(255, 255, 255, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Sn0wColor> doubleFill2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Double Fill2")
            .withValue(new Sn0wColor(255, 0, 11, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Sn0wColor> doubleLine2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Double Line2")
            .withValue(new Sn0wColor(255, 255, 255, 0))
            .withPage("Fade")
            .withPageParent(glowMode)
            .register(this);
    Value<Boolean> voidHoles = new ValueBuilder<Boolean>()
            .withDescriptor("Void Holes")
            .withValue(false)
            .register(this);
    Value<Sn0wColor> voidSafeFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Void Safe Fill")
            .withValue(new Sn0wColor(170, 0, 255, 25))
            .register(this);
    Value<Sn0wColor> voidSafeLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Void Safe Line")
            .withValue(new Sn0wColor(170, 0, 255, 255))
            .register(this);
    Value<Sn0wColor> voidFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Void Fill")
            .withValue(new Sn0wColor(255, 1, 242, 25))
            .register(this);
    Value<Sn0wColor> voidLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Void Line")
            .withValue(new Sn0wColor(255, 0, 251, 255))
            .register(this);
    Value<Number> voidHeight = new ValueBuilder<Number>()
            .withDescriptor("Void Height")
            .withValue(0.2)
            .withRange(0.1, 2.0)
            .register(this);

    ExecutorService service = Executors.newCachedThreadPool();

    volatile List<HoleUtils.Hole> holes = new ArrayList<>();
    volatile List<BlockPos> voidPositions = new ArrayList<>();

    public HoleEsp()
    {
        super("HoleESP", Category.Render);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        service.submit(() ->
        {
            holes = HoleUtils.getHoles(range.getValue().floatValue(), mc.player.getBlockPos(), doubles.getValue());
            if (voidHoles.getValue())
            {
                voidPositions = getVoidHoles();
            }
        });
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (holes.isEmpty()) return;



        for (HoleUtils.Hole hole : holes)
        {
            Box holeBB = hole.doubleHole ? new Box(hole.pos1.getX(), hole.pos1.getY(), hole.pos1.getZ(), hole.pos2.getX() + 1, hole.pos2.getY() + 1, hole.pos2.getZ() + 1) : new Box(hole.pos1);


            Color fillColor = hole.bedrock ? bedrockFill.getValue().getColor() : hole.doubleHole ? doubleFill.getValue().getColor() : obbyFill.getValue().getColor();
            Color fillColor2 = hole.bedrock ? bedrockFill2.getValue().getColor() : hole.doubleHole ? doubleFill2.getValue().getColor() : obbyFill2.getValue().getColor();
            Color outlineColor = hole.bedrock ? bedrockLine.getValue().getColor() : hole.doubleHole ? doubleLine.getValue().getColor() : obbyLine.getValue().getColor();
            Color outlineColor2 = hole.bedrock ? bedrockLine2.getValue().getColor() : hole.doubleHole ? doubleLine2.getValue().getColor() : obbyLine2.getValue().getColor();

            double renderHeight = height.getValue().doubleValue();


            if (!animation.getValue().equals("None"))
            {
                double fadeRange = range.getValue().floatValue() - 1.0;
                double rangeAnimation = MathHelper.clamp(((fadeRange + 1) - mc.player.getEyePos().distanceTo(holeBB.getCenter())) / fadeRange, 0.0, 1.0);


                switch (animation.getValue())
                {
                    case "Fade":
                        fillColor = ColorUtil.newAlpha(fillColor, (int) (fillColor.getAlpha() * rangeAnimation));
                        fillColor2 = ColorUtil.newAlpha(fillColor2, (int) (fillColor2.getAlpha() * rangeAnimation));
                        outlineColor = ColorUtil.newAlpha(outlineColor, (int) (outlineColor.getAlpha() * rangeAnimation));
                        outlineColor2 = ColorUtil.newAlpha(outlineColor2, (int) (outlineColor2.getAlpha() * rangeAnimation));
                        break;
                    case "Height":
                        renderHeight = height.getValue().doubleValue() * rangeAnimation;
                        if (renderHeight == 0) continue;

                        break;
                }
            }


            holeBB = new Box(holeBB.minX, holeBB.minY, holeBB.minZ, holeBB.maxX, holeBB.minY + renderHeight, holeBB.maxZ);


            switch (glowMode.getValue())
            {
                case "None":
                    RenderUtil.renderBox(
                            RenderType.FILL,
                            holeBB,
                            fillColor,
                            fillColor
                    );
                    RenderUtil.renderBox(
                            RenderType.LINES,
                            holeBB,
                            outlineColor,
                            outlineColor
                    );
                    break;
                case "Fade":
                    RenderUtil.renderBox(
                            RenderType.LINES,
                            holeBB,
                            outlineColor,
                            outlineColor2
                    );
                    RenderUtil.renderBox(
                            RenderType.FILL,
                            holeBB,
                            fillColor,
                            fillColor2
                    );
                    break;
            }
            /*   GL11.glLineWidth(1.0f);*/

        }

        if (voidHoles.getValue())
        {

            for (BlockPos pos : voidPositions)
            {
                boolean safe = mc.world.getBlockState(pos).getBlock() != Blocks.AIR;
                Color fill = safe ? voidSafeFill.getValue().getColor() : voidFill.getValue().getColor();
                Color line = safe ? voidSafeLine.getValue().getColor() : voidLine.getValue().getColor();

                Box bb = new Box(pos);
                bb = new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + voidHeight.getValue().floatValue(), bb.maxZ);


                RenderUtil.renderBox(
                        RenderType.FILL,
                        bb,
                        fill,
                        fill
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        bb,
                        line,
                        line
                );

            }
        }
    }

    public List<BlockPos> getVoidHoles()
    {
        List<BlockPos> voids = new ArrayList<>();

        for (int x = -range.getValue().intValue(); x < range.getValue().intValue(); x++)
        {
            for (int z = -range.getValue().intValue(); z < range.getValue().intValue(); z++)
            {
                BlockPos pos = new BlockPos((int) (mc.player.getX() + x), mc.world.getBottomY(), (int) (mc.player.getZ() + z));

                if (mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) voids.add(pos);
            }
        }
        return voids;
    }

    @Override
    public String getDescription()
    {
        return "HoleESP: see safe holes that prevents crystal damage";
    }


}