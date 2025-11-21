package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.impl.gui.ClickGui;

import java.awt.*;


public class ArmorHud extends HudComponent
{

    public ArmorHud()
    {
        super("ArmorHud");
        immovable = true;
    }

    Value<Boolean> percentIcon = new ValueBuilder<Boolean>()
            .withDescriptor("Percent")
            .withValue(false)
            .register(this);
    Value<Boolean> small = new ValueBuilder<Boolean>()
            .withDescriptor("Small")
            .withValue(false)
            .register(this);
    Value<Boolean> triColor = new ValueBuilder<Boolean>()
            .withDescriptor("Tri Color")
            .withValue(false)
            .register(this);
    public Value<Sn0wColor> armorColorA = new ValueBuilder<Sn0wColor>()
            .withDescriptor("High Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .withParent(triColor)
            .withParentEnabled(true)
            .register(this);
    public Value<Sn0wColor> armorColorB = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Middle Color")
            .withValue(new Sn0wColor(0, 0, 255, 255))
            .withParent(triColor)
            .withParentEnabled(true)
            .register(this);
    public Value<Sn0wColor> armorColorC = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Low Color")
            .withValue(new Sn0wColor(87, 8, 97))
            .withParent(triColor)
            .withParentEnabled(true)
            .register(this);

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);

        if (NullUtils.nullCheck() || renderCheck(event)) return;

        renderArmorHUD(event.getContext(), true);
    }



    public void renderArmorHUD(DrawContext context, boolean percent)
    {
        ScaledResolution resolution = new ScaledResolution(mc);
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - (mc.player.isSubmergedIn(FluidTags.WATER) ? 10 : 0);
        for (ItemStack is : mc.player.getInventory().armor)
        {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;

            context.drawItem(is, x, y);
            context.drawItemInSlot(mc.textRenderer, is, x, y);
            String s = (is.getCount() > 1) ? (is.getCount() + "") : "";
            Fonts.doOneText(context, s, (x + 19 - 2 - ClickGui.CONTEXT.getRenderer().getTextWidth(s)), (y + 9), HudColors.getTextColor(y + 9), true);
            if (percent)
            {
                float green = ((float) is.getMaxDamage() - (float) is.getDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                int dmg = 100 - (int) (red * 100);

                if(small.getValue())
                {
                    context.getMatrices().push();
                    context.getMatrices().scale(0.625f, 0.625f, 1.0f);
                    Fonts.doOneText(
                            context,
                            dmg + (percentIcon.getValue() ? "%" : ""),
                            ((x + 6) * 1.6f) - (ClickGui.CONTEXT.getRenderer().getTextWidth((dmg + (percentIcon.getValue() ? "%" : ""))) / 2.0f) * 0.6f,
                            (y * 1.6f) - 11,
                            getArmorColor(dmg, y + 9),
                            true);
                    context.getMatrices().pop();
                }else{
                    Fonts.doOneText(
                            context,
                            dmg + (percentIcon.getValue() ? "%" : ""),
                            (x + 8 - ClickGui.CONTEXT.getRenderer().getTextWidth((dmg + (percentIcon.getValue() ? "%" : ""))) / 2.0f),
                            y - 9,
                            getArmorColor(dmg, y + 9),
                            true);
                }
            }
        }
    }

    public Color getArmorColor(int dmg, int y)
    {
        if (triColor.getValue())
        {
            if (dmg < 50)
            {
                return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(dmg, 1, 50), 0, 1), armorColorB.getValue().getColor(), armorColorC.getValue().getColor());
            } else
            {
                return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(dmg, 50, 100), 0, 1), armorColorA.getValue().getColor(), armorColorB.getValue().getColor());
            }
        } else
        {
            return HudColors.getTextColor(y);
        }
    }

    @Override
    public String getDescription()
    {
        return "ArmorHud: Renders your Armor on screen";
    }
}
