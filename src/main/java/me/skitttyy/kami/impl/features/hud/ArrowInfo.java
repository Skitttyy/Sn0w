package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.gui.hudeditor.HudEditor;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;

import java.awt.*;
import java.time.ZonedDateTime;

public class ArrowInfo extends HudComponent {
    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());
            })
            .register(this);

    Value<Boolean> whenShooting = new ValueBuilder<Boolean>()
            .withDescriptor("Only Shooting")
            .withValue(false)
            .register(this);

    public static ArrowInfo INSTANCE;

    public ArrowInfo()
    {
        super("ArrowInfo");
        INSTANCE = this;
        this.time = ZonedDateTime.now();
    }

    ZonedDateTime time;
    Color color;

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);
        if (NullUtils.nullCheck() || renderCheck(event)) return;


        if (((!(mc.player.getMainHandStack().getItem().equals(Items.BOW) && mc.player.isUsingItem()) && whenShooting.getValue())) && !(mc.currentScreen instanceof HudEditor))
            return;


        color = Color.WHITE;
        String string = getArrowString();

        if (autoPos.getValue())
        {
            ScaledResolution sr = new ScaledResolution(mc);
            xPos.setValue(((sr.getScaledWidth() / 2) - 10) - ClickGui.CONTEXT.getRenderer().getTextWidth(string));
            yPos.setValue((sr.getScaledHeight() / 2) - 5);
        }
        width = (int) Fonts.getTextWidth(string);
        Fonts.renderText(event.getContext(),
                string,
                xPos.getValue().floatValue(),
                yPos.getValue().floatValue() + 1,
                color,
                FontModule.INSTANCE.textShadow.getValue());
    }


    String getArrowString()
    {
        for (int i = 0; i < 36; i++)
        {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() == Items.TIPPED_ARROW)
            {

                if (!itemStack.get(DataComponentTypes.POTION_CONTENTS).getEffects().iterator().hasNext()) continue;

                final StatusEffectInstance effect = itemStack.get(DataComponentTypes.POTION_CONTENTS).getEffects().iterator().next();


                color = ColorUtil.getLiquidColor(effect.getEffectType().getIdAsString().replace("minecraft:", ""), effect.getEffectType().value());
                return itemStack.getName().getString();
            } else if (itemStack.getItem() == Items.ARROW)
            {
                return "Arrow";
            }
        }
        return "None";
    }

    @Override
    public String getDescription()
    {
        return "ArrowInfo: Shows what arrow you are shooting";
    }
}
