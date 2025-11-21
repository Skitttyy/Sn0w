package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;

public class TotemCounter extends HudComponent {
    public TotemCounter()
    {
        super("TotemCounter");
    }

    Value<Boolean> autoPos = new ValueBuilder<Boolean>()
            .withDescriptor("Auto Pos")
            .withValue(true)
            .withAction(s ->
            {
                xPos.setActive(!s.getValue());
                yPos.setActive(!s.getValue());
            })
            .register(this);
    public Value<Sn0wColor> textColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Text Color")
            .withValue(new Sn0wColor(255, 255, 255))
            .register(this);

    @SubscribeEvent
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);
        if (NullUtils.nullCheck() || renderCheck(event)) return;


        this.width = 16;
        this.height = 16;
        ScaledResolution sr = new ScaledResolution(mc);


        if (autoPos.getValue())
        {
            int i = sr.getScaledWidth() / 2;
            xPos.setValue(i - 189 + 180 + 2);
            yPos.setValue(sr.getScaledHeight() - 55 - (mc.player.isSubmergedInWater() ? 10 : 0));
        }

        int count = InventoryUtils.getItemCount(Items.TOTEM_OF_UNDYING);
        if (count == 0) return;

        RenderUtil.renderItemWithCount(event.getContext(), new ItemStack(Items.TOTEM_OF_UNDYING), new Point(xPos.getValue().intValue(), yPos.getValue().intValue()), count, textColor.getValue().getColor(), false);
    }

    @Override
    public String getDescription()
    {
        return "TotemCounter: displays how many totems you have";
    }

}