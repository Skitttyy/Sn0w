package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoReplenish extends Module {
    Timer timer = new Timer();


    Value<Number> percent = new ValueBuilder<Number>()
            .withDescriptor("Percent")
            .withValue(32)
            .withRange(0, 100)
            .withPlaces(0)
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(200)
            .withRange(0, 500)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);

    public AutoReplenish()
    {
        super("AutoReplenish", Category.Misc);
    }

    Item[] cache = new Item[9];

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (percent.getValue().intValue() == 0)
        {
            if (mc.currentScreen != null)
            {
                for (int i = 0; i < 9; i++)
                {
                    cache[i] = mc.player.getInventory().getStack(i).getItem();
                }
                return;
            }

            if (mc.player.isDead())
            {
                cache = new Item[9];
                return;
            }

            int index = 0;
            if (timer.isPassed())
            {
                for (Item item : cache)
                {
                    if (item != null && !item.equals(Items.AIR))
                    {
                        if (mc.player.getInventory().getStack(index).isEmpty())
                        {
                            try
                            {
                                int slot = getSlot(item);
                                if (slot != -1)
                                {
                                    InventoryUtils.moveItem(slot, index);
                                    timer.resetDelay();
                                    return;
                                }
                            } catch (Exception ignored)
                            {
                            }
                        }
                    }
                    index++;
                }
            }
            for (int i = 0; i < 9; i++)
            {
                cache[i] = mc.player.getInventory().getStack(i).getItem();
            }
        } else
        {

            if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;

            if (timer.isPassed())
                for (int i = 0; i < 9; i++)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.isEmpty() || !stack.isStackable())
                    {
                        continue;
                    }
                    double stackPercent = ((float) stack.getCount() / stack.getMaxCount()) * 100.0f;
                    if (stack.getCount() == 1 || stackPercent <= Math.max(percent.getValue().floatValue(), 5.0f))
                    {
                        if (replenishStack(stack, i)) return;
                    }
                }
        }
    }

    int getSlot(Item item)
    {
        int slot = -1;
        for (int i = 45; i > 9; i--)
        {
            if (mc.player.getInventory().getStack(i).getItem().equals(item))
            {
                slot = i;
                break;
            }
        }

        return slot;
    }

    private boolean replenishStack(ItemStack item, int hotbarSlot)
    {


        int total = item.getCount();
        for (int i = 9; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            // We cannot merge stacks if they don't have the same name
            if (!stack.getName().equals(item.getName()))
            {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem && (!(item.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock()))
            {
                continue;
            }
            if (stack.getItem() != item.getItem())
            {
                continue;
            }
            if (total < stack.getMaxCount())
            {
                InventoryUtils.pickupSlot(i);
                InventoryUtils.pickupSlot(hotbarSlot + 36);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
                {
                    InventoryUtils.pickupSlot(i);
                }
                timer.resetDelay();
                return true;
            }
        }
        return false;

    }

    @Override
    public String getDescription()
    {
        return "AutoReplenish: Refills items in your hotbar when you run out of them";
    }

}
