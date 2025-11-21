package me.skitttyy.kami.impl.features.modules.render;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.impl.features.commands.AutoRegearCommand;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.PotionItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Formatting;

import java.util.*;

public class AutoRegear extends Module
{
    public AutoRegear()
    {
        super("AutoRegear", Category.Misc);
    }

    Value<Boolean> closeAfter = new ValueBuilder<Boolean>()
            .withDescriptor("Close")
            .withValue(false)
            .register(this);
    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(1)
            .withRange(1, 1000)
            .register(this);
    Value<Number> clicks = new ValueBuilder<Number>()
            .withDescriptor("Clicks")
            .withValue(1)
            .withPlaces(0)
            .withRange(1, 5)
            .register(this);
    private HashMap<Integer, String> expectedInv = new HashMap<>();

    Timer timer = new Timer();

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        setup();
    }


    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;

        if (expectedInv.isEmpty())
        {
            return;
        }
        int actions = 0;

        timer.setDelay(speed.getValue().intValue());
        ScreenHandler handler = mc.player.currentScreenHandler;

        if (handler.slots.size() != 63 && handler.slots.size() != 90)
            return;


        if (mc.currentScreen instanceof ShulkerBoxScreen)
        {
            if (timer.isPassed())
            {
                ArrayList<Integer> clickSequence = buildClickSequence(handler);
                for (int s : clickSequence)
                {
                    if (s >= 3000)
                    {
                        InventoryUtils.pickupSlot(s - 3000);
                    } else
                    {
                        InventoryUtils.pickupSlot(s);
                        actions++;
                        if (actions >= clicks.getValue().intValue())
                            break;
                    }
                }
                timer.resetDelay();

                if (clickSequence.isEmpty() && closeAfter.getValue())
                {
                    mc.currentScreen.close();
                }
            }
        }


    }


    public void setup()
    {
        String selectedKit = AutoRegearCommand.getCurrentSet();

        if (selectedKit.isEmpty())
        {
            ChatUtils.sendMessage(Formatting.RED + "No kit is selected! Use the kit command");
            return;
        }


        String kitItems = AutoRegearCommand.getInventoryKit(selectedKit);

        if (kitItems.isEmpty() || kitItems.split(" ").length != 36)
        {
            ChatUtils.sendMessage(Formatting.RED + "Invalid kit! create it again");
            return;
        }

        String[] items = kitItems.split(" ");
        expectedInv = new HashMap<>();

        for (int i = 0; i < 36; i++)
            if (!items[i].equals("block.minecraft.air"))
                expectedInv.put(i, items[i]);
    }


    private int searchInContainer(String name, boolean lower, ScreenHandler handler)
    {
        ItemStack cursorStack = handler.getCursorStack();

        if ((cursorStack.getItem() instanceof PotionItem ?
                cursorStack.getItem().getTranslationKey() + cursorStack.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS).getColor()
                : cursorStack.getItem().getTranslationKey()).equals(name))
            return -2;


        int bestSlot = -1;
        int bestCount = 0;
        for (int i = 0; i < (lower ? 26 : 53); i++)
        {
            ItemStack stack = handler.getSlot(i).getStack();
            if (((stack.getItem() instanceof PotionItem ? stack.getItem().getTranslationKey() + stack.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS).getColor() : stack.getItem().getTranslationKey()).equals(name))
                    && bestCount < stack.getCount())
            {
                bestCount = stack.getCount();
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private ArrayList<Integer> buildClickSequence(ScreenHandler handler)
    {
        ArrayList<Integer> clicks = new ArrayList<>();
        for (int s : expectedInv.keySet())
        {
            int lower = s < 9 ? s + 54 : s + 18;
            int upper = s < 9 ? s + 81 : s + 45;

            ItemStack itemInslot = handler.slots.get((handler.slots.size() == 63 ? lower : upper)).getStack();

            if ((itemInslot.getItem() instanceof PotionItem &&
                    (itemInslot.getItem().getTranslationKey() + itemInslot.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS).getColor()).equals(expectedInv.get(s))))
                continue;


            if (itemInslot.getItem().getTranslationKey().equals(expectedInv.get(s)))
            {

                int total = itemInslot.getCount();
                if (total != itemInslot.getMaxCount())
                {
                    int bestSlot = -1;
                    int bestCount = 0;
                    for (int i = 0; i < (handler.slots.size() == 63 ? 26 : 53); i++)
                    {
                        ItemStack stack = handler.slots.get(i).getStack();
                        // We cannot merge stacks if they don't have the same name
                        if (!stack.getName().equals(itemInslot.getName()))
                        {
                            continue;
                        }
                        if (stack.getItem() instanceof BlockItem blockItem && (!(itemInslot.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock()))
                        {
                            continue;
                        }
                        if (stack.getItem() != itemInslot.getItem())
                        {
                            continue;
                        }

                        if (total < stack.getMaxCount() && stack.getCount() > bestCount)
                        {
                            bestCount = stack.getCount();
                            bestSlot = i;
                            if (bestCount + total > itemInslot.getMaxCount())
                                break;
                        }
                    }
                    if (bestSlot != -1)
                    {
                        clicks.add(bestSlot + 3000);
                        clicks.add((handler.slots.size() == 63 ? lower : upper) + 3000);
                        clicks.add(bestSlot);
                    }
                }
                continue;
            }


            int slot = searchInContainer(expectedInv.get(s), handler.slots.size() == 63, handler);

            if (slot == -2)
            {
                clicks.add(handler.slots.size() == 63 ? lower : upper);
            } else if (slot != -1)
            {
                clicks.add(slot);
                clicks.add(handler.slots.size() == 63 ? lower : upper);
                clicks.add(slot);
            }
        }
        return clicks;
    }

    @Override
    public String getDescription()
    {
        return "AutoRegear: Automatically regears ur inventory with set kit";
    }

}
