package me.skitttyy.kami.impl.features.modules.player;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
    public static ChestStealer INSTANCE;
    Timer timer = new Timer();
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> timer.setDelay(set.getValue().longValue()))
            .register(this);

    Value<Boolean> close = new ValueBuilder<Boolean>()
            .withDescriptor("Close")
            .withValue(false)
            .register(this);

    public ChestStealer()
    {
        super("ChestStealer", Category.Player);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest)
        {

            if (timer.isPassed())
            {
                for (int i = 0; i < chest.getInventory().size(); i++)
                {
                    Slot slot = chest.getSlot(i);
                    if (slot.hasStack())
                    {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.resetDelay();
                        break;
                    }
                }
            }
            if (isContainerEmpty(chest) && close.getValue())
                mc.player.closeHandledScreen();
        }else{

        }
    }

    private boolean isContainerEmpty(GenericContainerScreenHandler container)
    {
        for (int i = 0; i < (container.getInventory().size() == 90 ? 54 : 27); i++)
            if (container.getSlot(i).hasStack()) return false;
        return true;
    }


    @Override
    public String getDescription()
    {
        return "ChestStealer: steals from chests";
    }
}