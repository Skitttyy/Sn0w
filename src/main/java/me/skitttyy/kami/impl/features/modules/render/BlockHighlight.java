package me.skitttyy.kami.impl.features.modules.render;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.impl.features.hud.FeatureList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.text.DecimalFormat;

public class BlockHighlight extends Module {
    public static BlockHighlight INSTANCE;

    Value<Sn0wColor> fill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill")
            .withValue(new Sn0wColor(255, 62, 62, 25))
            .register(this);
    Value<Sn0wColor> line = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line")
            .withValue(new Sn0wColor(255, 62, 62, 255))
            .register(this);
    Value<Boolean> entity = new ValueBuilder<Boolean>()
            .withDescriptor("Entity")
            .withValue(false)
            .register(this);
    Value<Sn0wColor> entityFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Entity Fill")
            .withValue(new Sn0wColor(255, 62, 62, 25))
            .withParentEnabled(true)
            .withParent(entity)
            .register(this);
    Value<Sn0wColor> entityLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Entity Line")
            .withValue(new Sn0wColor(255, 62, 62, 255))
            .withParent(entity)
            .withParentEnabled(true)

            .register(this);

    public BlockHighlight()
    {
        super("BlockHighlight", Category.Render);
        INSTANCE = this;
    }

    private double distance = 0;


    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        Box render = null;
        final HitResult result = mc.crosshairTarget;
        if (result != null)
        {
            final Vec3d pos = mc.player.getEyePos();
            if (entity.getValue()
                    && result.getType() == HitResult.Type.ENTITY)
            {
                final Entity entity = ((EntityHitResult) result).getEntity();
                render = entity.getBoundingBox();
                distance = pos.distanceTo(entity.getPos());
            } else if (result.getType() == HitResult.Type.BLOCK)
            {
                BlockPos hpos = ((BlockHitResult) result).getBlockPos();
                BlockState state = mc.world.getBlockState(hpos);
                VoxelShape outlineShape = state.getOutlineShape(mc.world, hpos);
                if (outlineShape.isEmpty())
                {
                    return;
                }
                Box render1 = outlineShape.getBoundingBox();
                render = new Box(hpos.getX() + render1.minX, hpos.getY() + render1.minY,
                        hpos.getZ() + render1.minZ, hpos.getX() + render1.maxX,
                        hpos.getY() + render1.maxY, hpos.getZ() + render1.maxZ);
                distance = pos.distanceTo(hpos.toCenterPos());
            }
        }
        if (render != null)
        {
            if (result.getType() == HitResult.Type.ENTITY)
            {
                RenderUtil.renderBox(RenderType.FILL, render, entityFill.getValue().getColor(), entityFill.getValue().getColor());
                RenderUtil.renderBox(RenderType.LINES, render, entityLine.getValue().getColor(), entityLine.getValue().getColor());
            } else
            {
                RenderUtil.renderBox(RenderType.FILL, render, fill.getValue().getColor(), fill.getValue().getColor());
                RenderUtil.renderBox(RenderType.LINES, render, line.getValue().getColor(), line.getValue().getColor());
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "BlockHighlight: highlights the block you are looking at";
    }


    @Override
    public String getHudInfo()
    {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(distance);
    }

}