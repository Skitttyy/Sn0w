package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.RainbowUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.combat.KillAura;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class CSGOInfo extends HudComponent {


    public CSGOInfo()
    {
        super("PvpInfo");
    }

    public Value<Sn0wColor> popColorA = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color A")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);
    public Value<Sn0wColor> popColorB = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color B")
            .withValue(new Sn0wColor(0, 0, 255, 255))
            .register(this);
    public Value<Sn0wColor> popColorC = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Color C")
            .withValue(new Sn0wColor(87, 8, 97))
            .register(this);

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);

        if (NullUtils.nullCheck() || renderCheck(event)) return;


        int height = (int) Fonts.getTextHeight("A");
        int i = 0;
        RainbowUtil.renderWave(event.getContext(), "catogod.cc", (float) xPos.getValue().intValue(), (float) yPos.getValue().intValue());
        PlayerEntity opp = (PlayerEntity) TargetUtils.getTarget(30);
        i += height;

        if (opp != null)
        {
            Fonts.doOneText(event.getContext(), "HTR", xPos.getValue().intValue(), yPos.getValue().intValue() + i, (mc.player.getEyePos().distanceTo(opp.getEyePos()) <= KillAura.INSTANCE.range.getValue().floatValue() ? Color.GREEN : Color.RED), ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            i += height;
            Fonts.doOneText(event.getContext(), "PLR", xPos.getValue().intValue(), yPos.getValue().intValue() + i, getHoleColor(opp), ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            i += height;
            if (opp.hasStatusEffect(StatusEffects.WEAKNESS))
            {
                Fonts.renderText(event.getContext(), "WKN", xPos.getValue().intValue(), yPos.getValue().intValue() + i, Color.GREEN, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
                i += height;

            }
        } else
        {
            Fonts.doOneText(event.getContext(), "HTR", xPos.getValue().intValue(), yPos.getValue().intValue() + i, Color.RED, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            i += height;
            Fonts.doOneText(event.getContext(), "PLR", xPos.getValue().intValue(), yPos.getValue().intValue() + i, Color.RED, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
            i += height;
        }
        int totems = InventoryUtils.getItemCount(Items.TOTEM_OF_UNDYING);

        Fonts.doOneText(event.getContext(), totems + "", xPos.getValue().intValue(), yPos.getValue().intValue() + i, getPopColor(totems), ClickGui.CONTEXT.getColorScheme().doesTextShadow());
        i += height;
        Fonts.doOneText(event.getContext(), "PING " +  PacketManager.INSTANCE.getClientLatency(), xPos.getValue().intValue(), yPos.getValue().intValue() + i, (PacketManager.INSTANCE.getClientLatency() <= 100 ? Color.GREEN : Color.RED), ClickGui.CONTEXT.getColorScheme().doesTextShadow());
        i += height;
        Fonts.doOneText(event.getContext(), "LBY", xPos.getValue().intValue(), yPos.getValue().intValue() + i, mc.player.getY() <= mc.world.getBottomY() + 1.0f ? Color.GREEN : Color.RED, ClickGui.CONTEXT.getColorScheme().doesTextShadow());
        this.width = ClickGui.CONTEXT.getRenderer().getTextWidth("catogod.cc");
        this.height = i + ClickGui.CONTEXT.getRenderer().getTextHeight("A");
    }

    public Color getHoleColor(PlayerEntity opp)
    {
        BlockPos pos = opp.getBlockPos();
        if (HoleUtils.isHole(pos))
        {
            if (HoleUtils.isObbyHole(pos))
            {
                return Color.ORANGE;
            } else if (HoleUtils.isBedrockHoles(pos))
            {
                return Color.GREEN;
            }
            return Color.RED;
        } else
        {
            return Color.RED;
        }
    }

    public Color getPopColor(int pops)
    {
        if (pops == 0) return Color.RED;


        if (pops < 5)
        {
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(pops, 1, 5), 0, 1), popColorB.getValue().getColor(), popColorA.getValue().getColor());
        } else
        {
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(pops, 5, 10), 0, 1), popColorC.getValue().getColor(), popColorB.getValue().getColor());
        }
    }

    @Override
    public String getDescription()
    {
        return "PvpInfo: nn thinks hes elite but he can be elite with this module";
    }
}
