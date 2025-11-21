package me.skitttyy.kami.impl.features.hud;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.FrameEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.TPSManager;
import me.skitttyy.kami.api.management.notification.types.component.TopComponent;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Info extends HudComponent
{

    public static Info INSTANCE;

    Value<String> alignment = new ValueBuilder<String>()
            .withDescriptor("Alignment")
            .withValue("BottomLeft")
            .withModes("BottomLeft", "BottomRight", "TopRight", "TopLeft")
            .register(this);
    Value<Boolean> potion = new ValueBuilder<Boolean>()
            .withDescriptor("Potions")
            .withValue(true)
            .register(this);
    Value<Boolean> potionColor = new ValueBuilder<Boolean>()
            .withDescriptor("Potion Color")
            .withValue(true)
            .withParent(potion)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> potionIcon = new ValueBuilder<Boolean>()
            .withDescriptor("Potion Icon")
            .withValue(true)
            .withParent(potion)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> potionLength = new ValueBuilder<Boolean>()
            .withDescriptor("Potions Sort")
            .withValue(true)
            .withParent(potion)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> items = new ValueBuilder<Boolean>()
            .withDescriptor("Items")
            .withValue(true)
            .register(this);
    Value<Boolean> brackets = new ValueBuilder<Boolean>()
            .withDescriptor("Brackets")
            .withValue(true)
            .register(this);
    Value<Boolean> tps = new ValueBuilder<Boolean>()
            .withDescriptor("Tps")
            .withValue(true)
            .withAction(s -> handleTPS(s.getValue()))
            .register(this);
    Value<Boolean> advancedTPS = new ValueBuilder<Boolean>()
            .withDescriptor("Advanced TPS")
            .withValue(true)
            .register(this);
    Value<Boolean> advancedTopsAstrix = new ValueBuilder<Boolean>()
            .withDescriptor("Advanced Astrix")
            .withValue(true)
            .register(this);
    Value<Boolean> lastResponse = new ValueBuilder<Boolean>()
            .withDescriptor("Second Length")
            .withValue(true)
            .register(this);
    Value<Boolean> coloredTPS = new ValueBuilder<Boolean>()
            .withDescriptor("Colored TPS")
            .withValue(true)
            .register(this);
    Value<Boolean> lagNotifier = new ValueBuilder<Boolean>()
            .withDescriptor("Lag Notifier")
            .withValue(true)
            .register(this);
    Value<Boolean> pearlTimer = new ValueBuilder<Boolean>()
            .withDescriptor("Pearl Cooldown")
            .withValue(true)
            .register(this);
    Value<Boolean> time = new ValueBuilder<Boolean>()
            .withDescriptor("Time")
            .withValue(true)
            .register(this);
    Value<Boolean> speed = new ValueBuilder<Boolean>()
            .withDescriptor("Speed")
            .withValue(true)
            .register(this);
    Value<Boolean> ping = new ValueBuilder<Boolean>()
            .withDescriptor("Ping")
            .withValue(true)
            .register(this);
    Value<Boolean> pingMS = new ValueBuilder<Boolean>()
            .withDescriptor("Ping MS")
            .withValue(true)
            .withParent(ping)
            .withParentEnabled(true)
            .register(this);
    Value<String> fpsMode = new ValueBuilder<String>()
            .withDescriptor("FPS")
            .withValue("None")
            .withModes("None", "Normal", "Fast")
            .register(this);
    Value<Boolean> durability = new ValueBuilder<Boolean>()
            .withDescriptor("Durability")
            .withValue(true)
            .register(this);
    Value<Boolean> scaleDura = new ValueBuilder<Boolean>()
            .withDescriptor("Scaled Dura")
            .withValue(true)
            .withParent(durability)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> brand = new ValueBuilder<Boolean>()
            .withDescriptor("Server Brand")
            .withValue(true)
            .register(this);
    Value<Boolean> players = new ValueBuilder<Boolean>()
            .withDescriptor("Players")
            .withValue(false)
            .register(this);
    Value<Boolean> serverIP = new ValueBuilder<Boolean>()
            .withDescriptor("IP")
            .withValue(false)
            .register(this);
    Value<Boolean> PPS = new ValueBuilder<Boolean>()
            .withDescriptor("PPS")
            .withValue(false)
            .register(this);
    Value<Boolean> lightLevel = new ValueBuilder<Boolean>()
            .withDescriptor("Light")
            .withValue(false)
            .register(this);
    Value<Boolean> saturation = new ValueBuilder<Boolean>()
            .withDescriptor("Saturation")
            .withValue(false)
            .register(this);
    Value<Boolean> worldTime = new ValueBuilder<Boolean>()
            .withDescriptor("World Time")
            .withValue(false)
            .register(this);
    Value<Boolean> advancedWorldTime = new ValueBuilder<Boolean>()
            .withDescriptor("ADVWorldTime")
            .withValue(false)
            .withParent(worldTime)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> dayCounter = new ValueBuilder<Boolean>()
            .withDescriptor("Day Counter")
            .withValue(false)
            .register(this);
    Value<Boolean> biome = new ValueBuilder<Boolean>()
            .withDescriptor("Biome")
            .withValue(false)
            .register(this);
    Value<Boolean> chests = new ValueBuilder<Boolean>()
            .withDescriptor("Chests")
            .withValue(false)
            .register(this);
    Value<Boolean> grayVals = new ValueBuilder<Boolean>()
            .withDescriptor("Gray Values")
            .withValue(false)
            .register(this);
    Value<Boolean> spacing = new ValueBuilder<Boolean>()
            .withDescriptor("Spacing")
            .withValue(false)
            .register(this);
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());

            })
            .register(this);
    DecimalFormat df = new DecimalFormat("0.0#");
    DecimalFormat dfSpeed = new DecimalFormat("0.00");
    DecimalFormat pearlFormat = new DecimalFormat("0.0");

    public TopComponent LAG_COMPONENT = new TopComponent("INVALID", 300L, new Color(255, 85, 85));
    public TopComponent PEARL_COMPONENT = new TopComponent("INVALID", 300L, new Color(255, 85, 255));

    public Info()
    {
        super("Info");
        INSTANCE = this;
    }

    void handleTPS(boolean show)
    {
        coloredTPS.setActive(show);
        advancedTPS.setActive(show);
        advancedTopsAstrix.setActive(show && advancedTPS.getValue());
        lastResponse.setActive(show);
    }

    private final Timer timer = new Timer();

    private long size = 0L;
    private long previousSize = 0L;
    private ChunkPos current = null;
    private final DecimalFormat format = new DecimalFormat("##.00#");
    protected final LinkedList<Long> frames = new LinkedList<>();
    int fastFpsCount = 0;
    int off = 0;


    private long serverLastUpdated = System.currentTimeMillis();
    private long pearlTime = 0;
    private boolean pearling;
    private boolean expectingPearl;


    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);


        if (autoPos.getValue())
        {
            ScaledResolution resolution = new ScaledResolution(mc);
            int resWidth = resolution.getScaledWidth();
            int resHeight = resolution.getScaledHeight();

            float bottom = resHeight - ClickGui.CONTEXT.getRenderer().getTextHeight("pPMCSWAG");
            Info.INSTANCE.xPos.setValue(resWidth - 1);
            Info.INSTANCE.yPos.setValue(bottom - 1);


        }
        if (NullUtils.nullCheck() || renderCheck(event)) return;


        List<InfoComponent> potions = new ArrayList<>();
        List<InfoComponent> info = new ArrayList<>();
        if (potion.getValue())
        {
            for (StatusEffectInstance e : mc.player.getStatusEffects())
            {
                final StatusEffect effect = e.getEffectType().value();

                boolean amplifier = e.getAmplifier() > 1 && !e.isInfinite();
                String text = effect.getName().getString() + " " + (amplifier ? e.getAmplifier() + " " : "") + Formatting.WHITE + StringUtils.getPotionDurationString(e, 1.0F);


                potions.add(new InfoComponent(ColorUtil.getLiquidColor(e.getEffectType().getIdAsString().replace("minecraft:", ""), effect), text, e));
            }
        }

        if (!(fpsMode.getValue().equals("None")))
            info.add(new InfoComponent(null, "FPS " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + (fpsMode.getValue().equals("Fast") ? fastFpsCount : mc.getCurrentFps())));

        if (ping.getValue())
            info.add(new InfoComponent(null, "Ping " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + PacketManager.INSTANCE.getClientLatency() + (pingMS.getValue() ? "ms" : "")));


        if (saturation.getValue())
            info.add(new InfoComponent(null, "Saturation " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + String.format("%.2f", mc.player.getHungerManager().getSaturationLevel())));
        if (lightLevel.getValue())
            info.add(new InfoComponent(null, "Light " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + String.format("%s", mc.world.getLightLevel(BlockPos.ofFloored(mc.player.getPos())))));

        if (PPS.getValue())
            info.add(new InfoComponent(null, "PPS " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + TPSManager.INSTANCE.getSendPackets() + "->" + TPSManager.INSTANCE.getIncPackets()));

        if (worldTime.getValue())
            info.add(new InfoComponent(null, "Phase " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + TPSManager.INSTANCE.getTimePhase() + (advancedWorldTime.getValue() ? Formatting.GRAY + " [" + (grayVals.getValue() ? "" : Formatting.WHITE) + TPSManager.INSTANCE.getTimeOverTps() + Formatting.GRAY + "]" : "")));

        if (dayCounter.getValue())
            info.add(new InfoComponent(null, "Day " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + TPSManager.INSTANCE.getDay()));


        if (chests.getValue())
            info.add(new InfoComponent(null, "Chests " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + getChestCount()));


        if (durability.getValue())
        {
            ItemStack held = mc.player.getMainHandStack();

            if (!held.isEmpty() && held.isDamageable())
            {
                int heldDura = held.getMaxDamage() - held.getDamage();
                float scaledDurability = (heldDura / (float) held.getMaxDamage()) * 100;

                Formatting damage = Formatting.GREEN;

                if (scaledDurability <= 80 && scaledDurability > 60)
                {
                    damage = Formatting.DARK_GREEN;
                } else if (scaledDurability <= 60 && scaledDurability > 40)
                {
                    damage = Formatting.YELLOW;
                } else if (scaledDurability <= 40 && scaledDurability > 20)
                {
                    damage = Formatting.GOLD;
                } else if (scaledDurability <= 20 && scaledDurability > 10)
                {
                    damage = Formatting.RED;
                } else if (scaledDurability <= 10)
                {
                    damage = Formatting.DARK_RED;
                }
                if (scaleDura.getValue()) heldDura = (int) scaledDurability;

                info.add(new InfoComponent(null, "Durability " + damage + heldDura + (scaleDura.getValue() ? "%" : "")));
            }
        }
        if (mc.getNetworkHandler().getConnection() != null)
        {
            if (mc.getNetworkHandler().getConnection().getAddress() != null && serverIP.getValue() && mc.player != null)
            {
                String ip = (mc.isInSingleplayer() || mc.getNetworkHandler() == null) ? "Singleplayer" : mc.getNetworkHandler().getConnection().getAddress().toString().toLowerCase();

                info.add(new InfoComponent(null, "IP " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + ip));
            }
            if (players.getValue())
            {
                int count = 0;
                if (mc.player.networkHandler.getConnection() == null || mc.isInSingleplayer())
                {
                    count = 1;
                } else if (mc.player.networkHandler.getConnection() != null)
                {
                    count = mc.player.networkHandler.getPlayerList().size();
                }

                info.add(new InfoComponent(null, "Players " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + count));
            }

            if (tps.getValue())
            {
                String tps = "TPS " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + df.format(TPSManager.INSTANCE.getTickRate()) + (advancedTPS.getValue() ? (advancedTopsAstrix.getValue() ? "*" : "") + " " + Formatting.GRAY + "[" + getTpsColor(TPSManager.INSTANCE.getAverage()) + df.format(TPSManager.INSTANCE.getAverage()) + Formatting.GRAY + "]" : "");

                if (TPSManager.INSTANCE.getLastResponse() != null && lastResponse.getValue())
                {
                    tps += Formatting.GRAY + " [" + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + MathUtil.round((System.currentTimeMillis() - TPSManager.INSTANCE.getLastResponse()) / 1000.0, 1) + "s" + Formatting.GRAY + "]";
                }
                info.add(new InfoComponent(null, tps));
            }

        }
        int crystals = InventoryUtils.getItemCount(Items.END_CRYSTAL);
        int totems = InventoryUtils.getItemCount(Items.TOTEM_OF_UNDYING);
        int exp = InventoryUtils.getItemCount(Items.EXPERIENCE_BOTTLE);
        if (items.getValue())
        {
            info.add(new InfoComponent(null, "Crystals " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + crystals));
            info.add(new InfoComponent(null, "Totems " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + totems));
            info.add(new InfoComponent(null, "Exp " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + exp));
        }

        if (time.getValue())
        {


            info.add(new InfoComponent(null, "Time " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + (new SimpleDateFormat("h:mm a")).format(new Date())));
        }

        if (biome.getValue())
            info.add(new InfoComponent(null, "Biome " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + mc.world.getBiome(BlockPos.ofFloored(mc.player.getPos())).getIdAsString()));
        if (speed.getValue())
            info.add(new InfoComponent(null, "Speed " + (grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE) + this.dfSpeed.format(MathHelper.sqrt((float) (Math.pow(coordsDiff('x'), 2.0) + Math.pow(coordsDiff('z'), 2.0))) / 0.05 * 3.6) + " km/h"));

        if (brand.getValue())
        {
            if (mc.getNetworkHandler().getConnection() != null && mc.getServer() != null && mc.getServer().getVersion() != null)
            {
                info.add(new InfoComponent(null, mc.getServer().getVersion()));
            }

        }

        info.sort(Comparator.comparingInt(i -> -ClickGui.CONTEXT.getRenderer().getTextWidth(i.text)));
        if (potionLength.getValue())
        {
            potions.sort(Comparator.comparingInt(i -> -ClickGui.CONTEXT.getRenderer().getTextWidth(i.text)));
        }
        off = (mc.currentScreen instanceof ChatScreen && alignment.getValue().contains("Bottom")) ? -14 : 0;

        if (!info.isEmpty() && info.get(0) != null)
        {
            int textWidth = ClickGui.CONTEXT.getRenderer().getTextWidth(info.get(0).text);
            width = alignment.getValue().contains("Right") ? -textWidth : textWidth;
            height = alignment.getValue().contains("Bottom") ? -((ClickGui.CONTEXT.getRenderer().getTextHeight("AA") + (spacing.getValue() ? 1 : 0)) * ((info.size() + potions.size()))) : ((ClickGui.CONTEXT.getRenderer().getTextHeight("AA") + (spacing.getValue() ? 1 : 0)) * info.size());

        } else
        {
            width = alignment.getValue().contains("Right") ? -30 : 30;
            height = alignment.getValue().contains("Bottom") ? -ClickGui.CONTEXT.getRenderer().getTextHeight("AA") : ClickGui.CONTEXT.getRenderer().getTextHeight("AA");
        }


        if (potion.getValue())
        {
            renderPotions(event.getContext(), potions);
        }
        renderInfo(event.getContext(), info);

        if (lagNotifier.getValue())
        {
            if ((1000L <= System.currentTimeMillis() - serverLastUpdated) && !(mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)))
            {
                String lag = Formatting.RED + "Server hasnt responded for " + timeDifference() + "s!";
                LAG_COMPONENT.setText(lag);
                LAG_COMPONENT.setVisible(true);
            } else
            {
                LAG_COMPONENT.setVisible(false);
            }
        } else
        {
            LAG_COMPONENT.setVisible(false);
        }

        if (pearlTimer.getValue() && mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL))
        {
            float progress = mc.player.getItemCooldownManager().getCooldownProgress(Items.ENDER_PEARL, 0);

            PEARL_COMPONENT.setText("Ender Pearl Cooldown (" + pearlFormat.format(progress) + "s)");
            PEARL_COMPONENT.setVisible(true);
        } else
        {
            PEARL_COMPONENT.setVisible(false);
        }
//        {
//
//
//            if (timePearlDifference() < 0)
//            {
//                pearling = false;
//            } else
//            {
//                String pearl = Formatting.LIGHT_PURPLE + "Ender Pearl Cooldown (" + timePearlDifference() + "s)";
//                if (!(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)))
//                {
//                    int divider = getScale();
//                    int pearlX = mc.displayWidth / divider / 2 - ClickGui.CONTEXT.getRenderer().getTextWidth(pearl) / 2;
//                    int pearlY = 12 + ClickGui.CONTEXT.getRenderer().getTextHeight(pearl);
//                    ClickGui.CONTEXT.getRenderer().renderText(
//                            pearl,
//                            pearlX,
//                            pearlY,
//                            HudColors.getTextColor(pearlY),
//                            ClickGui.CONTEXT.getColorScheme().doesTextShadow()
//                    );
//                }
//            }
//        }
    }

    public String getChestCount()
    {
        int singleCount = 0;
        int doubleCount = 0;

        for (BlockEntity be : BlockUtils.getBlockEntities())
        {
            if (be instanceof ChestBlockEntity chest)
            {
                ChestType chestType = mc.world.getBlockState(chest.getPos()).get(ChestBlock.CHEST_TYPE);
                if (chestType == ChestType.SINGLE)
                {
                    singleCount++;
                    if (singleCount >= 2)
                    {
                        doubleCount++;
                        singleCount = 0;
                    }
                } else doubleCount++;
            }
        }

        double doubles = doubleCount;

        double singles = singleCount;

        DecimalFormat format = new DecimalFormat("#.#");
        String text = format.format(doubles);

        if (singles != 0)
        {
            text = text + ":" + format.format(singles);
        }
        return text;
    }


    @SubscribeEvent
    public void onFrameFlip(FrameEvent.FrameFlipEvent event)
    {

        if (!fpsMode.getValue().equals("Fast")) return;

        long time = System.nanoTime();

        frames.add(time);

        while (true)
        {
            long firstFrame = frames.getFirst();
            final long second = 1000000L * 1000L;
            if (time - firstFrame > second) frames.remove();
            else break;
        }

        fastFpsCount = frames.size();
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event)
    {
        serverLastUpdated = System.currentTimeMillis();


        if (NullUtils.nullCheck()) return;
    }

    private double timeDifference()
    {
        return MathUtil.round((System.currentTimeMillis() - serverLastUpdated) / 1000d, 1);
    }

    private double timePearlDifference()
    {
        return MathUtil.round((pearlTime - System.currentTimeMillis()) / 1000d, 1);
    }


    public void renderPotions(DrawContext context, List<InfoComponent> potions)
    {
        StatusEffectSpriteManager statusEffectSpriteManager = mc.getStatusEffectSpriteManager();


        for (InfoComponent comp : potions)
        {

            int x = alignment.getValue().contains("Right") ? (xPos.getValue().intValue() - ClickGui.CONTEXT.getRenderer().getTextWidth(comp.text)) : xPos.getValue().intValue();
            Fonts.doOneText(
                    context,
                    comp.text,
                    x,
                    yPos.getValue().intValue() + off,
                    (potionColor.getValue() ? comp.color : HudColors.getTextColor(yPos.getValue().intValue() + off)),
                    ClickGui.CONTEXT.getColorScheme().doesTextShadow()
            );
            float height = alignment.getValue().contains("Top") ? ClickGui.CONTEXT.getRenderer().getTextHeight(comp.text) : -ClickGui.CONTEXT.getRenderer().getTextHeight(comp.text);


            if (potionIcon.getValue() && comp.effect.shouldShowIcon())
            {

                int iconX = (xPos.getValue().intValue() + (alignment.getValue().contains("Right") ? -ClickGui.CONTEXT.getRenderer().getTextWidth(comp.text) : ClickGui.CONTEXT.getRenderer().getTextWidth(comp.text)) + (alignment.getValue().contains("Right") ? -10 : 1));

                Sprite sprite = statusEffectSpriteManager.getSprite(comp.effect.getEffectType());

                context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
                context.drawSprite(iconX, (yPos.getValue().intValue() + off), 0, 9, 9, sprite);
                context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            off += height;
            if (spacing.getValue())
                off += alignment.getValue().contains("Top") ? 1 : -1;
        }
    }


    public void renderInfo(DrawContext context, List<InfoComponent> info)
    {
        for (InfoComponent comp : info)
        {
            int x = alignment.getValue().contains("Right") ? (xPos.getValue().intValue() - ClickGui.CONTEXT.getRenderer().getTextWidth(comp.text)) : xPos.getValue().intValue();

            Fonts.doOneText(
                    context,
                    comp.text,
                    x,
                    yPos.getValue().intValue() + off,
                    HudColors.getTextColor(yPos.getValue().intValue() + off),
                    ClickGui.CONTEXT.getColorScheme().doesTextShadow()
            );

            off += alignment.getValue().contains("Top") ? ClickGui.CONTEXT.getRenderer().getTextHeight(comp.text) : -ClickGui.CONTEXT.getRenderer().getTextHeight(comp.text);
            if (spacing.getValue())
                off += alignment.getValue().contains("Top") ? 1 : -1;
        }
    }

    static class InfoComponent
    {
        Color color;
        String text;
        StatusEffectInstance effect;

        public InfoComponent(Color color, String text)
        {
            this.color = color;
            this.text = text;
            effect = null;
        }

        public InfoComponent(Color color, String text, StatusEffectInstance effect)
        {
            this.color = color;
            this.text = text;
            this.effect = effect;
        }

    }

    public static double coordsDiff(final char s)
    {
        switch (s)
        {
            case 'x':
            {
                return mc.player.getX() - mc.player.prevX;
            }
            case 'z':
            {
                return mc.player.getZ() - mc.player.prevZ;
            }
            default:
            {
                return 0.0;
            }
        }
    }

    public Formatting getTpsColor(double tps)
    {
        if (coloredTPS.getValue())
        {
            if (tps >= 18)
            {
                return Formatting.GREEN;
            }
            if (tps >= 16)
            {
                return Formatting.DARK_GREEN;
            }
            if (tps >= 12)
            {
                return Formatting.YELLOW;
            }

            return Formatting.RED;
        } else
        {
            return grayVals.getValue() ? Formatting.GRAY : Formatting.WHITE;
        }

    }

    private static String toFormattedBytes(long size)
    {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        if (size < 1000) // less than 1KB
        {
            return format.format(size) + "B";
        } else if (size < 1000000) // less than 1MB
        {
            return format.format((double) size / 1000.D) + "Kb";
        } else
        {
            return format.format((double) size / 1000000.D) + "Mb";
        }
    }

    public String getDisplayText()
    {
        return String.format(
                "%s | %s",
                size == -1 ? "error" : toFormattedBytes(size), difference(size - previousSize));
    }

    private static String difference(long size)
    {
        if (size == 0)
        {
            return "+0 B";
        }
        if (size > 0)
        {
            return "+" + toFormattedBytes(size);
        } else
        {
            return "-" + toFormattedBytes(Math.abs(size));
        }
    }

    @Override
    public String getDescription()
    {
        return "Info: Displays various information";
    }
}
