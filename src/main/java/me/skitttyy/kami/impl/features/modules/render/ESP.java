package me.skitttyy.kami.impl.features.modules.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Getter;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.player.TeamColorEvent;
import me.skitttyy.kami.api.event.events.render.EntityOutlineEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.breaks.BreakManager;
import me.skitttyy.kami.api.management.breaks.data.BreakData;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.utils.world.EntityUtils;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.mixin.accessor.IWorldRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ESP extends Module
{

    private final Map<PlayerEntity, BlockPos> burrowedPlayers = new HashMap<PlayerEntity, BlockPos>();
    ;
    Value<String> page = new ValueBuilder<String>()
            .withDescriptor("Page")
            .withValue("Basic")
            .withModes("Basic", "Break", "Items", "Chorus", "Burrows", "Crawl", "Pearl")
            .register(this);

    // Basic
    Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(false)
            .withPageParent(page)
            .withPage("Basic")
            .register(this);
    Value<Boolean> animals = new ValueBuilder<Boolean>()
            .withDescriptor("Animals")
            .withValue(true)
            .withPageParent(page)
            .withPage("Basic")
            .register(this);
    Value<Boolean> monsters = new ValueBuilder<Boolean>()
            .withDescriptor("Monsters")
            .withValue(true)
            .withPageParent(page)
            .withPage("Basic")
            .register(this);
    Value<Boolean> vehicles = new ValueBuilder<Boolean>()
            .withDescriptor("Rideables")
            .withValue(false)
            .withPageParent(page)
            .withPage("Basic")
            .register(this);

    // Break
    Value<Boolean> breakESP = new ValueBuilder<Boolean>()
            .withDescriptor("Breaks")
            .withValue(false)
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Boolean> predictBreak = new ValueBuilder<Boolean>()
            .withDescriptor("Predictions")
            .withValue(false)
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Number> breakRange = new ValueBuilder<Number>()
            .withDescriptor("Range", "breakRange")
            .withValue(6)
            .withRange(1d, 15.0d)
            .withPlaces(1)
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Sn0wColor> breakFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill", "breakFillStart")
            .withValue(new Sn0wColor(255, 0, 0, 81))
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Sn0wColor> breakLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line", "breakLineStart")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Sn0wColor> breakFill2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Ready Fill", "breakFillReady")
            .withValue(new Sn0wColor(0, 255, 0, 81))
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    Value<Sn0wColor> breakLine2 = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Ready Line", "breakLineFinished")
            .withValue(new Sn0wColor(0, 255, 0, 255))
            .withPageParent(page)
            .withPage("Break")
            .register(this);
    // Break
    Value<Boolean> itemTags = new ValueBuilder<Boolean>()
            .withDescriptor("Nametags", "itemNametag")
            .withValue(false)
            .withPageParent(page)
            .withPage("Items")
            .register(this);
    Value<Boolean> itemBox = new ValueBuilder<Boolean>()
            .withDescriptor("Item Box", "itemBox")
            .withValue(false)
            .withPageParent(page)
            .withPage("Items")
            .register(this);
    Value<Sn0wColor> itemFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill", "itemFill")
            .withValue(new Sn0wColor(0, 0, 255, 25))
            .withPageParent(page)
            .withPage("Items")
            .register(this);
    Value<Sn0wColor> itemLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line", "itemLine")
            .withValue(new Sn0wColor(0, 0, 255, 255))
            .withPageParent(page)
            .withPage("Items")
            .register(this);
    Value<Boolean> chorus = new ValueBuilder<Boolean>()
            .withDescriptor("Chorus")
            .withValue(false)
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<String> chorusMode = new ValueBuilder<String>()
            .withDescriptor("Mode", "chorusMode")
            .withValue("Tag")
            .withModes("Tag", "BoundingBox")
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<Sn0wColor> chorusFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill", "chorusFill")
            .withValue(new Sn0wColor(255, 0, 255, 25))
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<Sn0wColor> chorusLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line", "chorusLine")
            .withValue(new Sn0wColor(255, 0, 255, 255))
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<Number> fadeStart = new ValueBuilder<Number>()
            .withDescriptor("Fade Start")
            .withValue(500)
            .withRange(1, 5000)
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<Number> fadeTime = new ValueBuilder<Number>()
            .withDescriptor("Fade Time")
            .withValue(500)
            .withRange(1, 2000)
            .withPageParent(page)
            .withPage("Chorus")
            .register(this);
    Value<Boolean> crawlEsp = new ValueBuilder<Boolean>()
            .withDescriptor("Crawls")
            .withValue(false)
            .withPageParent(page)
            .withPage("Crawl")
            .register(this);
    Value<Sn0wColor> crawlFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill", "crawlFill")
            .withValue(new Sn0wColor(0, 0, 255, 25))
            .withPageParent(page)
            .withPage("Crawl")
            .register(this);
    Value<Sn0wColor> crawlLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line", "crawlLine")
            .withValue(new Sn0wColor(0, 0, 255, 255))
            .withPageParent(page)
            .withPage("Crawl")
            .register(this);
    Value<Boolean> pearls = new ValueBuilder<Boolean>()
            .withDescriptor("Pearls")
            .withValue(false)
            .withPageParent(page)
            .withPage("Pearl")
            .register(this);

    //Burrow
    Value<Boolean> burrowESP = new ValueBuilder<Boolean>()
            .withDescriptor("Burrows")
            .withValue(false)
            .withPageParent(page)
            .withPage("Burrows")
            .register(this);
    Value<Sn0wColor> burrowFill = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Fill", "burrowFill")
            .withValue(new Sn0wColor(255, 62, 62, 25))
            .withPageParent(page)
            .withPage("Burrows")
            .register(this);
    Value<Sn0wColor> burrowLine = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Line", "burrowLine")
            .withValue(new Sn0wColor(255, 62, 62, 255))
            .withPageParent(page)
            .withPage("Burrows")
            .register(this);


    public ESP()
    {
        super("ESP", Category.Render);
    }

    public static List<ChorusPos> chorusPosList = new CopyOnWriteArrayList<>();


    Map<Integer, String> pearlTags = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (burrowESP.getValue())
        {
            getPlayers();
        }

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (breakESP.getValue())
        {

            if (!predictBreak.getValue())
            {
                Int2ObjectMap<BlockBreakingInfo> blockBreakProgressions =
                        ((IWorldRenderer) mc.worldRenderer).getBlockBreakingProgressions();
                for (Int2ObjectMap.Entry<BlockBreakingInfo> info :
                        Int2ObjectMaps.fastIterable(blockBreakProgressions))
                {
                    BlockPos pos = info.getValue().getPos();
                    double dist = mc.player.squaredDistanceTo(pos.toCenterPos());
                    if (dist > MathUtil.square(breakRange.getValue().floatValue()))
                    {
                        continue;
                    }
                    int damage = info.getValue().getStage();
                    BlockState state = mc.world.getBlockState(pos);
                    VoxelShape outlineShape = state.getOutlineShape(mc.world, pos);
                    if (outlineShape.isEmpty())
                    {
                        continue;
                    }
                    Box bb = outlineShape.getBoundingBox();
                    bb = new Box(pos.getX() + bb.minX, pos.getY() + bb.minY,
                            pos.getZ() + bb.minZ, pos.getX() + bb.maxX, pos.getY() + bb.maxY, pos.getZ() + bb.maxZ);
                    double x = bb.minX + (bb.maxX - bb.minX) / 2.0;
                    double y = bb.minY + (bb.maxY - bb.minY) / 2.0;
                    double z = bb.minZ + (bb.maxZ - bb.minZ) / 2.0;
                    double sizeX = damage * ((bb.maxX - x) / 9.0);
                    double sizeY = damage * ((bb.maxY - y) / 9.0);
                    double sizeZ = damage * ((bb.maxZ - z) / 9.0);
                    Box render = new Box(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ);
                    RenderUtil.renderBox(
                            RenderType.FILL,
                            render,
                            breakFill.getValue().getColor(),
                            breakFill.getValue().getColor()
                    );

                    RenderUtil.renderBox(
                            RenderType.LINES,
                            render,
                            breakLine.getValue().getColor(),
                            breakLine.getValue().getColor()
                    );
                }
            } else
            {
                for (BreakData data : BreakManager.INSTANCE.getBreakDatas())
                {
                    if (data.canRender())
                        renderData(data);
                }
            }
        }
        if (chorus.getValue() && !chorusPosList.isEmpty())
        {
            for (ChorusPos pos : chorusPosList)
            {
                int lineA = chorusFill.getValue().getColor().getAlpha();
                int fillA = chorusLine.getValue().getColor().getAlpha();
                int tagA = 255;
                if (System.currentTimeMillis() - pos.startTime > fadeStart.getValue().longValue())
                {
                    long time = System.currentTimeMillis() - pos.startTime - fadeStart.getValue().longValue();
                    double normal = MathUtil.normalize(((double) time), 0, fadeTime.getValue().doubleValue());
                    normal = MathHelper.clamp(normal, 0, 1);
                    normal = (-normal) + 1;
                    lineA = (int) (normal * lineA);
                    fillA = (int) (normal * fillA);
                    tagA = (int) (normal * tagA);
                }
                if (lineA == 0 && fillA == 0)
                {
                    chorusPosList.remove(pos);
                    continue;
                }
                Color fill = ColorUtil.newAlpha(chorusFill.getValue().getColor(), fillA);
                Color line = ColorUtil.newAlpha(chorusLine.getValue().getColor(), lineA);

                if (chorusMode.getValue().equals("Tag"))
                {
                    RenderBuffers.schedulePreRender(() ->
                    {
                        RenderUtil.drawText("*chorus*", new Vec3d(pos.x, pos.y, pos.z), 2);
                    });
                } else if (chorusMode.getValue().equals("BoundingBox"))
                {
                    RenderUtil.renderBox(
                            RenderType.FILL,
                            pos.bb,
                            fill,
                            fill
                    );

                    RenderUtil.renderBox(
                            RenderType.LINES,
                            pos.bb,
                            line,
                            line
                    );
                }
            }
        }
        if (itemTags.getValue() || itemBox.getValue())
        {
            for (Entity entity : mc.world.getEntities())
            {
                if (!(entity instanceof ItemEntity) || !entity.isAlive())
                {
                    continue;
                }
                if (entity.distanceTo(mc.player) <= 20)
                {
                    Box bb = entity.getBoundingBox();

                    if (itemBox.getValue())
                    {
                        RenderUtil.renderBox(
                                RenderType.FILL,
                                bb,
                                itemFill.getValue().getColor(),
                                itemFill.getValue().getColor()
                        );

                        RenderUtil.renderBox(
                                RenderType.LINES,
                                bb,
                                itemLine.getValue().getColor(),
                                itemLine.getValue().getColor()
                        );
                    }
                    if (itemTags.getValue())
                    {
                        RenderBuffers.schedulePreRender(() ->
                        {
                            RenderUtil.drawText(entity.getDisplayName().getString() + (((((ItemEntity) entity).getStack().getCount() != 1) ? " x" + ((ItemEntity) entity).getStack().getCount() : "")), bb.getCenter(), 1.4f);
                        });
                    }
                }
            }
        }

        if (crawlEsp.getValue() && mc.player.isCrawling())
        {
            BlockPos playerPos = mc.player.getBlockPos();


            for (Direction direction : Direction.values())
            {
                if (direction.getAxis().isVertical()) continue;

                BlockPos pos = playerPos.offset(direction);

                if (mc.world.getBlockState(pos).isReplaceable()) continue;

                if (mc.world.getBlockState(pos.up()).isAir() && mc.world.getBlockState(pos.up().up()).isAir())
                {
                    Box box = new Box(pos);

                    box = new Box(box.minX, box.maxY - 0.2D, box.minZ, box.maxX, box.maxY, box.maxZ);

                    RenderUtil.renderBox(
                            RenderType.FILL,
                            box,
                            crawlFill.getValue().getColor(),
                            crawlFill.getValue().getColor()
                    );
                    RenderUtil.renderBox(
                            RenderType.LINES,
                            box,
                            crawlLine.getValue().getColor(),
                            crawlLine.getValue().getColor()
                    );
                }
            }
        }

        if (pearls.getValue())
        {
            for (Map.Entry<Integer, String> tag : pearlTags.entrySet())
            {

                Entity entity = mc.world.getEntityById(tag.getKey());


                if (!(entity instanceof EnderPearlEntity))
                {
                    pearlTags.remove(tag.getKey());
                    continue;
                }

                Box bb = Interpolator.getInterpolatedEntityBox(entity);
                RenderBuffers.schedulePreRender(() ->
                {
                    RenderUtil.drawText(tag.getValue(), bb.getCenter(), 1.6f);
                });
            }
        }

        if (burrowESP.getValue())
        {
            for (Map.Entry<PlayerEntity, BlockPos> entry : burrowedPlayers.entrySet())
            {
                Box box = new Box(entry.getValue());
                RenderUtil.renderBox(
                        RenderType.FILL,
                        box,
                        burrowFill.getValue().getColor(),
                        burrowFill.getValue().getColor()
                );
                RenderUtil.renderBox(
                        RenderType.LINES,
                        box,
                        burrowLine.getValue().getColor(),
                        burrowLine.getValue().getColor()
                );
            }
        }
    }

    private void getPlayers()
    {
        burrowedPlayers.clear();
        for (PlayerEntity player : mc.world.getPlayers())
        {
            if (player != mc.player && !FriendManager.INSTANCE.isFriend(player))
            {
                if (!HoleUtils.isBurrowed(player))
                {
                    continue;
                }
                burrowedPlayers.put(player, player.getBlockPos());
            }
        }
    }

    public void renderData(BreakData data)
    {

        float maxBreak = 1.0f;


        float color = maxBreak - 0.3f;
        BlockPos mining = data.getPos();
        VoxelShape outlineShape = VoxelShapes.fullCube();
        outlineShape = BlockUtils.getBlockState(data.getPos()).getOutlineShape(mc.world, mining);
        outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;

        Box render1 = outlineShape.getBoundingBox();
        Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
        Vec3d center = render.getCenter();

        float scale = MathHelper.clamp((System.currentTimeMillis() - data.getTimeout().getStartTime()) / 1000.0f, 0, 1.0f);
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

        Color fillColorInterp = breakFill.getValue().getColor();
        Color lineColorInterp = breakLine.getValue().getColor();
        if (data.getBestDamage() > color)
        {
            float progress = (float) MathHelper.clamp(MathUtil.normalize(data.getBestDamage() - color, 0, 0.3), 0, 1);
            fillColorInterp = ColorUtil.interpolate(progress, breakFill2.getValue().getColor(), fillColorInterp);
            lineColorInterp = ColorUtil.interpolate(progress, breakLine2.getValue().getColor(), lineColorInterp);
        }
        RenderUtil.renderBox(
                RenderType.FILL,
                scaled,
                fillColorInterp,
                fillColorInterp
        );
        RenderUtil.renderBox(
                RenderType.LINES,
                scaled,
                lineColorInterp,
                lineColorInterp
        );
    }

    @SubscribeEvent
    public void onSpawnEntity(EntityEvent.Add event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getEntity() instanceof EnderPearlEntity pearl)
        {

            Entity player = mc.world.getClosestPlayer(event.getEntity(), 3.0);
            if (player != null)
            {
                pearlTags.put(pearl.getId(), player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public void onRemoveEntity(EntityEvent.Remove event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getEntity() instanceof EnderPearlEntity pearl)
        {
            if (pearlTags.get(pearl.getId()) != null)
                pearlTags.remove(pearl.getId());
        }
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof PlaySoundS2CPacket packet && chorus.getValue())
        {
            if (packet.getSound().value() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT)
            {
                Box playerBB = mc.player.getBoundingBox();
                double widthX = (playerBB.maxX - playerBB.minX) / 2;
                double height = playerBB.maxY - playerBB.minY;
                double widthZ = (playerBB.maxZ - playerBB.minZ) / 2;
                Box renderBB = new Box(
                        packet.getX() - widthX,
                        packet.getY(),
                        packet.getZ() - widthZ,
                        packet.getX() + widthX,
                        packet.getY() + height,
                        packet.getZ() + widthZ
                );
                chorusPosList.add(new ChorusPos(renderBB, System.currentTimeMillis(), packet.getX(), packet.getY(), packet.getZ()));
            }
        }
    }

    @SubscribeEvent
    public void onEntityOutline(EntityOutlineEvent event)
    {
        if (checkESP(event.getEntity()))
        {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onTeamColor(TeamColorEvent event)
    {
        if (checkESP(event.getEntity()))
        {
            event.setCancelled(true);
            event.setColor(getESPColor(event.getEntity()).getRGB());
        }
    }


    public Color getESPColor(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            return HudColors.getTextColor(0);
        }
        if (EntityUtils.isMonster(entity))
        {
            return Color.RED;
        }
        if (EntityUtils.isNeutral(entity) || EntityUtils.isPassive(entity))
        {
            return Color.GREEN;
        }

        return null;
    }

    public boolean checkESP(Entity entity)
    {
        return entity != mc.player
                && entity instanceof PlayerEntity && players.getValue()
                || EntityUtils.isMonster(entity) && monsters.getValue()
                || (EntityUtils.isNeutral(entity) || EntityUtils.isPassive(entity)) && animals.getValue()
                || EntityUtils.isVehicle(entity) && vehicles.getValue();
    }

    private double getProgress(double delta)
    {
        return 1 - Math.pow(1 - (delta), 5);
    }

    private Box getBox(BlockPos pos, double progress)
    {
        return new Box(pos.getX() + 0.5 - progress / 2, pos.getY() + 0.5 - progress / 2, pos.getZ() + 0.5 - progress / 2, pos.getX() + 0.5 + progress / 2, pos.getY() + 0.5 + progress / 2, pos.getZ() + 0.5 + progress / 2);
    }

    @Override
    public String getDescription()
    {
        return "ESP: see stuff you wouldnt be normally able to see and stuff";
    }

    class ChorusPos
    {
        public Box bb;
        public long startTime;
        public double x, y, z;

        public ChorusPos(Box bb, long startTime, double x, double y, double z)
        {
            this.bb = bb;
            this.startTime = startTime;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }


}