package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoTotem extends Module {

    Value<Boolean> instaSwap = new ValueBuilder<Boolean>()
            .withDescriptor("Fast")
            .withValue(false)
            .register(this);
    Value<String> item = new ValueBuilder<String>()
            .withDescriptor("Item")
            .withValue("Totem")
            .withModes("Totem", "Gapple", "Crystal")
            .register(this);
    Value<Number> totemHealth = new ValueBuilder<Number>()
            .withDescriptor("Totem Health")
            .withValue(15)
            .withPlaces(1)
            .withRange(0, 36)
            .register(this);
    Value<Boolean> swordGap = new ValueBuilder<Boolean>()
            .withDescriptor("Sword Gap")
            .withValue(false)
            .register(this);
    Value<Number> totemSwordGap = new ValueBuilder<Number>()
            .withDescriptor("Sword Health")
            .withValue(15)
            .withRange(0, 36)
            .withPlaces(1)
            .withParent(swordGap)
            .withParentEnabled(true)
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 300)
            .register(this);

    public AutoTotem()
    {
        super("AutoTotem", Category.Combat);
    }

    Timer swapTimer = new Timer();
    int stage;
    int currentSlot = -1;

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        swapTimer.setDelay(delay.getValue().longValue());

        if (mc.currentScreen instanceof GenericContainerScreen && !(mc.currentScreen instanceof InventoryScreen))
        {
            return;
        }
        Item item = getItem();
        if (mc.player.getOffHandStack().getItem() != item && swapTimer.isPassed())
        {
            InventoryUtils.moveItemToOffhand(getItem(), instaSwap.getValue());
            swapTimer.resetDelay();
        }
    }


    public Item getItem()
    {
        if (swordGap.getValue() && (mc.player.getInventory().getMainHandStack().getItem().equals(Items.DIAMOND_SWORD) || mc.player.getInventory().getMainHandStack().getItem().equals(Items.NETHERITE_SWORD)) && mc.options.useKey.isPressed() && mc.currentScreen == null && mc.player.getHealth() + mc.player.getAbsorptionAmount() > totemSwordGap.getValue().doubleValue())
        {
            return Items.ENCHANTED_GOLDEN_APPLE;
        } else if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > totemHealth.getValue().doubleValue())
        {
            return getSelectedItem();
        }

        return Items.TOTEM_OF_UNDYING;
    }

    public Item getSelectedItem()
    {
        Item i = null;
        if (item.getValue().equals("Totem"))
        {
            i = Items.TOTEM_OF_UNDYING;
        } else if (item.getValue().equals("Crystal"))
        {
            i = Items.END_CRYSTAL;
        } else if (item.getValue().equals("Gapple"))
        {
            i = Items.ENCHANTED_GOLDEN_APPLE;
        }

        return i;
    }

    @Override
    public String getHudInfo()
    {
        if (NullUtils.nullCheck()) return "";

        return InventoryUtils.getItemCount(Items.TOTEM_OF_UNDYING) + "";
    }

    @Override
    public String getDescription()
    {
        return "AutoTotem: Attempts to put totem/whatever item into ur offhand when needed";
    }
}
