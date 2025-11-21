package me.skitttyy.kami.impl.features.hud;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderGameOverlayEvent;
import me.skitttyy.kami.api.feature.hud.HudComponent;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.notification.types.component.TopComponent;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.render.ScaledResolution;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.gui.ClickGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArmorWarner extends HudComponent {
    public static ArmorWarner INSTANCE;

    public ArmorWarner()
    {
        super("ArmorWarner");
        INSTANCE = this;

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
    Value<Number> armorThreshold = new ValueBuilder<Number>()
            .withDescriptor("Threshold")
            .withValue(50)
            .withRange(1, 100)
            .withPlaces(0)
            .register(this);


    private final Map<PlayerEntity, Integer> entityArmorArraylist = new HashMap<>();
    public TopComponent HELMET_COMPONENT = new TopComponent("INVALID", 300L, Color.RED);
    public TopComponent CHESTPLATE_COMPONENT = new TopComponent("INVALID", 300L, Color.RED);
    public TopComponent LEGGINGS_COMPONENT = new TopComponent("INVALID", 300L, Color.RED);
    public TopComponent BOOTS_COMPONENT = new TopComponent("INVALID", 300L, Color.RED);

    @SubscribeEvent
    @Override
    public void draw(RenderGameOverlayEvent.Text event)
    {
        super.draw(event);

        if (NullUtils.nullCheck() || renderCheck(event)) return;

        immovable = true;
        this.width = 0;
        this.height = 0;

        List<TopComponent> list = new ArrayList<>();
        for (ItemStack stack : mc.player.getInventory().armor)
        {
            if (stack == ItemStack.EMPTY) continue;


            String armor = InventoryUtils.getArmorPieceName(stack);
            if (armor == null) continue;


            float green = ((float) stack.getMaxDamage() - (float) stack.getDamage()) / (float) stack.getMaxDamage();
            float red = 1 - green;
            int dmg = 100 - (int) (red * 100);



            TopComponent component = getComponentByName(armor);
            if (dmg <= armorThreshold.getValue().intValue() && component != null)
            {
                String text = "Your " + armor + (armor.endsWith("s") ? " are" : " is") + " at " + dmg + "%!";
                component.setText(text);
                component.setVisible(true);
                list.add(component);
            }
        }
        if (!list.contains(HELMET_COMPONENT))
            HELMET_COMPONENT.setVisible(false);

        if (!list.contains(CHESTPLATE_COMPONENT))
            CHESTPLATE_COMPONENT.setVisible(false);
        if (!list.contains(LEGGINGS_COMPONENT))
            LEGGINGS_COMPONENT.setVisible(false);
        if (!list.contains(BOOTS_COMPONENT))
            BOOTS_COMPONENT.setVisible(false);
    }

    @Override
    public String getDescription()
    {
        return "ArmorWarner: Warns you and possibly your friends if armor gonna break";
    }


    public TopComponent getComponentByName(String name)
    {
        switch (name)
        {
            case "helmet" ->
            {
                return HELMET_COMPONENT;
            }
            case "chest" ->
            {
                return CHESTPLATE_COMPONENT;
            }
            case "leggings" ->
            {
                return LEGGINGS_COMPONENT;
            }
            case "boots" ->
            {
                return BOOTS_COMPONENT;
            }

        }
        return null;
    }
}
