package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.RenderBlockEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.SearchManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;


import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Search extends Module {
    private ArrayList<Block> defaultBlocks;


    Value<Sn0wColor> defaultColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color")
            .withValue(new Sn0wColor(255, 0, 251, 255))
            .register(this);
    Value<Boolean> tracers = new ValueBuilder<Boolean>()
            .withDescriptor("Tracers")
            .withValue(false)
            .withParentEnabled(false)
            .register(this);
    Value<Number> lineWidth = new ValueBuilder<Number>()
            .withDescriptor("Width")
            .withValue(0.8)
            .withRange(0.1, 2)
            .withParent(tracers)
            .withParentEnabled(true)
            .register(this);

    public static Search INSTANCE;

    private CopyOnWriteArrayList<Pair<BlockPos, BlockState>> foundBlocks = new CopyOnWriteArrayList<>();


    public Search()
    {
        super("Search", Category.Render);
        INSTANCE = this;
    }

    @Override
    public String getHudInfo()
    {
        return tracers.getValue() ? "Lines" : "Fill";
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (foundBlocks.isEmpty()) return;

        foundBlocks.removeIf(e -> mc.world.getBlockState(e.key()).getBlock() != e.value().getBlock() || !SearchManager.INSTANCE.hasBlock(e.value().getBlock()));


    }

    @SubscribeEvent
    public void onRenderBlock(RenderBlockEvent event)
    {
        if (SearchManager.INSTANCE.hasBlock(event.getState().getBlock()) && foundBlocks.stream().noneMatch(pair -> pair.key().equals(event.getPos())))
        {
            foundBlocks.add(new Pair<>(new BlockPos(event.getPos()), event.getState()));
        }
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (foundBlocks.isEmpty()) return;

        for (Pair<BlockPos, BlockState> pair : foundBlocks)
        {

            Color blockColor = getColor(pair.value().getBlock());
            BlockPos pos = pair.key();
            Box renderBB = new Box(pos);
            RenderUtil.renderBox(
                    RenderType.FILL,
                    renderBB,
                    ColorUtil.newAlpha(blockColor, defaultColor.getValue().getColor().getAlpha()),
                    ColorUtil.newAlpha(blockColor, defaultColor.getValue().getColor().getAlpha())
            );
            RenderUtil.renderBox(
                    RenderType.LINES,
                    renderBB,
                    ColorUtil.newAlpha(blockColor, 255),
                    ColorUtil.newAlpha(blockColor, 255)
            );
            if (tracers.getValue())
                RenderUtil.renderTracerLine(mc.gameRenderer.getCamera().getPos(), renderBB.getCenter(), ColorUtil.newAlpha(blockColor, 255), ColorUtil.newAlpha(blockColor, 255), lineWidth.getValue().floatValue());
        }

    }


    public Color getColor(Block block)
    {
        if (block == Blocks.OBSIDIAN)
        {
            return new Color(90, 51, 160);
        }
        if (block == Blocks.CRYING_OBSIDIAN)
        {
            return new Color(0, 0, 100).brighter();
        }
        if (block == Blocks.NETHER_PORTAL)
        {
            return new Color(87, 41, 255);
        }
        if (block == Blocks.ENDER_CHEST)
        {
            return new Color(90, 51, 160).brighter().brighter();
        }
        if (block == Blocks.CHEST)
        {
            return new Color(200, 120, 50);
        }
        if (block == Blocks.TRAPPED_CHEST)
        {
            return new Color(200, 0, 50);
        }
        if (block == Blocks.LEVER)
        {
            return new Color(255, 0, 0);
        }
        if (block == Blocks.COBWEB)
        {
            return new Color(255, 255, 255);
        }

        if (block == Blocks.FURNACE)
        {
            return new Color(100, 100, 100);
        }

        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)
        {
            return Color.CYAN;
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)
        {
            return new Color(255, 215, 0);
        }
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)
        {
            return new Color(161, 157, 148);
        }
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)
        {
            return Color.BLUE;
        }
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)
        {
            return Color.RED;
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)
        {
            return Color.ORANGE;
        }
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)
        {
            return Color.GREEN;
        }
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)
        {
            return Color.BLACK;
        }
        if (block == Blocks.NETHER_QUARTZ_ORE)
        {
            return Color.WHITE;
        }

        if (block instanceof ShulkerBoxBlock)
        {
            return new Color(((ShulkerBoxBlock) block).getColor().getEntityColor());
        }

        return defaultColor.getValue().getColor();
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        mc.worldRenderer.reload();
    }


    @Override
    public String getDescription()
    {
        return "Search: highlight any block in the world! for example: \"-search add deepslate_diamond_ore\"";
    }

}
