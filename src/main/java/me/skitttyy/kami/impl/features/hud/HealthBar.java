package me.skitttyy.kami.impl.features.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.GUI;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.mixin.accessor.IIngameHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.text.DecimalFormat;

public class HealthBar extends HudComponent
{
    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Vanilla")
            .withModes("Vanilla", "Bar", "None")
            .register(this);
    Value<String> barColor = new ValueBuilder<String>()
            .withDescriptor("Bar Color")
            .withValue("Scissor")
            .withModes("Scissor", "Custom")
            .withPageParent(mode)
            .withPage("Bar")
            .register(this);
    Value<Number> barThickness = new ValueBuilder<Number>()
            .withDescriptor("Thickness")
            .withValue(1)
            .withRange(0.5f, 15)
            .withPlaces(1)
            .withPageParent(mode)
            .withPage("Bar")
            .register(this);
    Value<Sn0wColor> leftColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Left Color")
            .withValue(new Sn0wColor(255, 0, 255, 255))
            .withPageParent(mode)
            .withPage("Bar")
            .register(this);
    Value<Sn0wColor> rightColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Right Color")
            .withValue(new Sn0wColor(0, 255, 0))
            .withPageParent(mode)
            .withPage("Bar")
            .register(this);


    Value<Boolean> text = new ValueBuilder<Boolean>()
            .withDescriptor("Text")
            .withValue(false)
            .register(this);
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
            })
            .register(this);

    public static HealthBar INSTANCE;

    public HealthBar()
    {
        super("HealthBar");
        INSTANCE = this;
    }


    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);
        if (NullUtils.nullCheck() || renderCheck(event)) return;

        if (mc.currentScreen instanceof GUI) return;


        width = 81;


        switch (mode.getValue())
        {
            case "Vanilla":
                if (autoPos.getValue())
                    xPos.setValue(((float) (event.getContext().getScaledWindowWidth() / 2) - (this.width / 2)) - 1);

                int i = MathHelper.ceil(mc.player.getHealth());
                int j = ((IIngameHud) mc.inGameHud).getRenderHealthValue();


                boolean bl = ((IIngameHud) mc.inGameHud).getHeartJumpEndTick() > (long) ((IIngameHud) mc.inGameHud).getTicks() && (((IIngameHud) mc.inGameHud).getHeartJumpEndTick() - (long) ((IIngameHud) mc.inGameHud).getTicks()) / 3L % 2L == 1L;


                float f = Math.max((float) mc.player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float) Math.max(j, i));
                int o = 0;
                int p = MathHelper.ceil((f + (float) o) / 2.0F / 10.0F);
                int q = Math.max(10 - (p - 2), 3);

                int s = -1;
                if (mc.player.hasStatusEffect(StatusEffects.REGENERATION))
                {
                    s = ((IIngameHud) mc.inGameHud).getTicks() % MathHelper.ceil(f + 5.0F);
                }

                ((IIngameHud) mc.inGameHud).doRenderHealth(event.getContext(), mc.player, xPos.getValue().intValue(), yPos.getValue().intValue(), q, s, f, i, j, o, bl);
                if (text.getValue())
                {
                    Color healthColor = getHealthColor();

                    float x = xPos.getValue().intValue() + (float) width / 2;
                    float y = yPos.getValue().intValue() + 10;

                    float health = mc.player.getHealth() == 0 ? 0 : mc.player.getHealth() / 2;
                    String color = PlayerUtils.getHealthColor(mc.player, false, false) + "";

                    DecimalFormat format = new DecimalFormat("#.#");

                    String text = color + format.format(health);
                    float offset = Fonts.getTextWidth(text);


                    float totalWidth = offset + 7;
                    boolean absorption = mc.player.getAbsorptionAmount() != 0;
                    String absorptionText = "";
                    if (absorption)
                    {
                        absorptionText = Formatting.GOLD + " " + format.format(mc.player.getAbsorptionAmount() / 2);
                        totalWidth += Fonts.getTextWidth(absorptionText);
                        totalWidth += 7;

                    }


                    Fonts.doOneText(event.getContext(), text, x - (totalWidth / 2), y, healthColor, FontModule.INSTANCE.textShadow.getValue());
                    RenderSystem.enableBlend();
                    event.getContext().drawGuiTexture(InGameHud.HeartType.NORMAL.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y), 7, 7);
                    RenderSystem.disableBlend();
                    offset += 7;
                    if (absorption)
                    {
                        Fonts.doOneText(event.getContext(), absorptionText, x - (totalWidth / 2) + offset, y, healthColor, FontModule.INSTANCE.textShadow.getValue());
                        offset += Fonts.getTextWidth(absorptionText);
                        RenderSystem.enableBlend();
                        event.getContext().drawGuiTexture(InGameHud.HeartType.ABSORBING.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y), 7, 7);
                        RenderSystem.disableBlend();
                    }
                }

                break;
            case "None":
                if(text.getValue())
                {
                    if (autoPos.getValue())
                        xPos.setValue(((float) (event.getContext().getScaledWindowWidth() / 2) - (this.width / 2)) - 1);

                    Color healthColor = getHealthColor();

                    float x = xPos.getValue().intValue() + (float) width / 2;
                    float y = yPos.getValue().intValue();

                    float health = mc.player.getHealth() == 0 ? 0 : mc.player.getHealth() / 2;
                    String color = PlayerUtils.getHealthColor(mc.player, false, false) + "";

                    DecimalFormat format = new DecimalFormat("#.#");

                    String text = color + format.format(health);
                    float offset = Fonts.getTextWidth(text);


                    float totalWidth = offset + 7;
                    boolean absorption = mc.player.getAbsorptionAmount() != 0;
                    String absorptionText = "";
                    if (absorption)
                    {
                        absorptionText = Formatting.GOLD + " " + format.format(mc.player.getAbsorptionAmount() / 2);
                        totalWidth += Fonts.getTextWidth(absorptionText);
                        totalWidth += 7;

                    }


                    Fonts.doOneText(event.getContext(), text, x - (totalWidth / 2), y, healthColor, FontModule.INSTANCE.textShadow.getValue());
                    RenderSystem.enableBlend();
                    event.getContext().drawGuiTexture(InGameHud.HeartType.NORMAL.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y), 7, 7);
                    RenderSystem.disableBlend();
                    offset += 7;
                    if (absorption)
                    {
                        Fonts.doOneText(event.getContext(), absorptionText, x - (totalWidth / 2) + offset, y, healthColor, FontModule.INSTANCE.textShadow.getValue());
                        offset += Fonts.getTextWidth(absorptionText);
                        RenderSystem.enableBlend();
                        event.getContext().drawGuiTexture(InGameHud.HeartType.ABSORBING.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y), 7, 7);
                        RenderSystem.disableBlend();
                    }
                }
                break;
            case "Bar":


                float thickness = barThickness.getValue().floatValue();
                float percentage = Math.min(1f, mc.player.getHealth() / mc.player.getMaxHealth());

                float width = 80.0F;
                float half = width / 2;


                if (autoPos.getValue())
                    xPos.setValue(((float) (event.getContext().getScaledWindowWidth() / 2) - (width / 2)) - 1);


                float x = xPos.getValue().intValue() + half + 2;
                float y = yPos.getValue().intValue() + 5;

                Color healthColor = getHealthColor();

                RenderUtil.renderRect(event.getContext().getMatrices(), (x - half - 0.5), (y - 0.5), (half * 2) + 1f, thickness + 1, 0x78000000);
                RenderUtil.renderGradient(event.getContext().getMatrices(), (x - half - 0.5), (y - 0.5), width * percentage + 1, thickness + 1,
                        barColor.getValue().equals("Scissor") ? Color.RED.darker().getRGB() : leftColor.getValue().getColor().darker().getRGB(), barColor.getValue().equals("Scissor") ? healthColor.darker().getRGB() : rightColor.getValue().getColor().darker().getRGB(), true);
                RenderUtil.renderGradient(event.getContext().getMatrices(), (x - half), y, width * percentage, thickness,
                        barColor.getValue().equals("Scissor") ? Color.RED.getRGB() : leftColor.getValue().getColor().getRGB(), barColor.getValue().equals("Scissor") ? healthColor.getRGB() : rightColor.getValue().getColor().getRGB(), true);


                if (text.getValue())
                {
                    float health = mc.player.getHealth() == 0 ? 0 : mc.player.getHealth() / 2;
                    String color = PlayerUtils.getHealthColor(mc.player, false, false).toString();

                    DecimalFormat format = new DecimalFormat("#.#");

                    String text = color + format.format(health);
                    float offset = Fonts.getTextWidth(text);


                    float totalWidth = offset + 7;
                    boolean absorption = mc.player.getAbsorptionAmount() != 0;
                    String absorptionText = "";
                    if (absorption)
                    {
                        absorptionText = Formatting.GOLD + " " + format.format(mc.player.getAbsorptionAmount() / 2);
                        totalWidth += Fonts.getTextWidth(absorptionText);
                        totalWidth += 7;

                    }


                    Fonts.doOneText(event.getContext(), text, x - (totalWidth / 2), y + thickness + 4, healthColor, FontModule.INSTANCE.textShadow.getValue());
                    RenderSystem.enableBlend();
                    event.getContext().drawGuiTexture(InGameHud.HeartType.NORMAL.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y + thickness + 4), 7, 7);
                    RenderSystem.disableBlend();
                    offset += 7;
                    if (absorption)
                    {
                        Fonts.doOneText(event.getContext(), absorptionText, x - (totalWidth / 2) + offset, y + thickness + 4, healthColor, FontModule.INSTANCE.textShadow.getValue());
                        offset += Fonts.getTextWidth(absorptionText);
                        RenderSystem.enableBlend();
                        event.getContext().drawGuiTexture(InGameHud.HeartType.ABSORBING.getTexture(false, false, false), (int) (x - (totalWidth / 2) + offset), (int) (y + thickness + 4), 7, 7);
                        RenderSystem.disableBlend();
                    }
                }
                break;
        }
    }

    private Color getHealthColor()
    {
        float f2 = mc.player.getMaxHealth();
        float f3 = Math.max(0.0f, Math.min(mc.player.getHealth(), f2) / f2);
        return new Color(Color.HSBtoRGB(f3 / 3.0f, 1.0f, 1.0f) | 0xFF000000);
    }

    @Override
    public String getDescription()
    {
        return "HealthBar: Draws a health bars";
    }
}
