package me.skitttyy.kami.impl.features.modules.combat;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.Utils32k;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.datafixer.fix.ItemShulkerBoxColorFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class AntiRevert extends Module {
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 10)
            .register(this);
    Value<Boolean> replenish = new ValueBuilder<Boolean>()
            .withDescriptor("Refill")
            .withValue(false)
            .register(this);

    public AntiRevert()
    {
        super("AntiRevert", Category.Combat);
    }

    int waitTicks;


    @SubscribeEvent
    public void onUpdate(final TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        int slot;
        if (waitTicks > delay.getValue().intValue() && (slot = Utils32k.findReverted32ks()) != -1)
        {
            waitTicks = 0;
            try
            {
                InventoryUtils.throwSlot(InventoryUtils.hotbarToInventory(slot));
                int shulker = -1;
                if (replenish.getValue() && (shulker = getSlot()) != -1)
                {
                    InventoryUtils.moveItem(shulker, slot);
                }
            } catch (Exception e)
            {
            }
        }
        waitTicks++;
    }

    int getSlot()
    {
        int slot = -1;
        for (int i = 45; i > 9; i--)
        {
            if (is32kShulker(mc.player.getInventory().getStack(i)))
            {
                slot = i;
                break;
            }
        }

        return slot;
    }

    private boolean is32kShulker(ItemStack stack)
    {
        if (stack.contains(DataComponentTypes.CONTAINER))
        {
            ContainerComponent containerComponent = stack.get(DataComponentTypes.CONTAINER);
            List<ItemStack> items = containerComponent.stream().toList();
            if (!items.isEmpty())
            {
                for (ItemStack item : items)
                {
                    if (item.isEmpty()) continue;
                    // The first non-empty slot in the shulker must be a 32k since that is what the hopper will dispense first.
                    return Utils32k.checkSharpness(item);
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        return "AntiRevert: Drops reverted 32ks / normal swords";
    }
}
