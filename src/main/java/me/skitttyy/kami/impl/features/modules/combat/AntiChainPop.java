package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.ScreenEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.world.CrystalUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.movement.NoSlow;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AntiChainPop extends Module
{
    Value<Boolean> hurtTime = new ValueBuilder<Boolean>()
            .withDescriptor("Hurt")
            .withValue(false)
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(0)
            .withRange(0, 300)
            .withParent(hurtTime)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> swapBack = new ValueBuilder<Boolean>()
            .withDescriptor("Swap Back")
            .withValue(false)
            .register(this);
    Value<Number> health = new ValueBuilder<Number>()
            .withDescriptor("Health")
            .withValue(15)
            .withPlaces(1)
            .withRange(0, 36)
            .register(this);
    Value<Boolean> autoRefill = new ValueBuilder<Boolean>()
            .withDescriptor("Refill")
            .withValue(false)
            .register(this);
    Value<Number> refillSlot = new ValueBuilder<Number>()
            .withDescriptor("Slot")
            .withValue(1)
            .withRange(1, 9)
            .withPlaces(0)
            .withParent(autoRefill)
            .withParentEnabled(true)
            .register(this);
    Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(0)
            .withRange(0, 300)
            .withParent(autoRefill)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> onlyLethal = new ValueBuilder<Boolean>()
            .withDescriptor("Only-Lethal")
            .withValue(false)
            .register(this);
    Value<Number> maxSelfDmg = new ValueBuilder<Number>()
            .withDescriptor("Max Damage")
            .withValue(4)
            .withRange(0, 36)
            .register(this);
    Value<Boolean> pause = new ValueBuilder<Boolean>()
            .withDescriptor("Pause")
            .withValue(false)
            .register(this);
    Value<Boolean> inventory = new ValueBuilder<Boolean>()
            .withDescriptor("Inventory")
            .withValue(false)
            .register(this);
    Value<Boolean> openTotem = new ValueBuilder<Boolean>()
            .withDescriptor("Open-Totem")
            .withValue(false)
            .register(this);

    public AntiChainPop()
    {
        super("AntiChainPop", Category.Combat);
    }

    Timer crystalTimer = new Timer();

    Timer swapBackTimer = new Timer();

    Timer refillTimer = new Timer();

    boolean lastHurtTime = true;
    int oldSlot = -1;

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        if (pause.getValue() && mc.player.isUsingItem() && mc.player.getActiveHand().equals(Hand.MAIN_HAND))
        {
            oldSlot = -1;
            return;
        }
        if (!inventory.getValue() && NoSlow.INSTANCE.canInvMove())
            return;

        crystalTimer.setDelay(delay.getValue().longValue());
        swapBackTimer.setDelay(250);
        refillTimer.setDelay(speed.getValue().longValue());

        int totemSlot = InventoryUtils.getHotbarItemSlot(Items.TOTEM_OF_UNDYING);


        doRefill();


        boolean curHurtTime = mc.player.hurtTime != 0;


        if (delay.getValue().intValue() != 0)
        {
            if (!lastHurtTime && curHurtTime)
            {
                crystalTimer.resetDelay();
            }
        }
        lastHurtTime = curHurtTime;


        if (hurtTime.getValue())
            if ((delay.getValue().intValue() != 0 && !crystalTimer.isPassed()))
            {
                return;
            }


        boolean lethal = true;

        //thx cwhack
        if (onlyLethal.getValue())
        {
            lethal = false;
            List<EndCrystalEntity> crystals = getNearByCrystals();
            ArrayList<Vec3d> crystalsPos = new ArrayList<>();
            crystals.forEach(e -> crystalsPos.add(e.getPos()));

            for (Vec3d pos : crystalsPos)
            {
                double damage = CrystalUtil.calculateDamage(mc.player, pos, false, false);

                if (damage > maxSelfDmg.getValue().floatValue() || damage + 2 > mc.player.getHealth() + mc.player.getAbsorptionAmount())
                {
                    lethal = true;
                    break;
                }
            }
        }

        if (totemSlot != -1 && (curHurtTime || !hurtTime.getValue()) && mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue().longValue() && lethal)
        {
            if (mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                oldSlot = mc.player.getInventory().selectedSlot;


            InventoryUtils.switchToSlot(totemSlot);
            swapBackTimer.resetDelay();
        } else
        {
            if (swapBack.getValue() && swapBackTimer.isPassed() && oldSlot != -1)
            {
                InventoryUtils.switchToSlot(oldSlot);
                oldSlot = -1;
            } else if (oldSlot != -1 && totemSlot != -1)
            {
                if (mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                    oldSlot = mc.player.getInventory().selectedSlot;


                InventoryUtils.switchToSlot(totemSlot);
            }
        }
    }

    private List<EndCrystalEntity> getNearByCrystals()
    {
        Vec3d pos = mc.player.getPos();
        return mc.world.getEntitiesByClass(EndCrystalEntity.class, new Box(pos.add(-4, -4, -4), pos.add(4, 4, 4)), a -> true);
    }


    public void doRefill()
    {

        if (!autoRefill.getValue()) return;


        if (!refillTimer.isPassed()) return;

        int slot = refillSlot.getValue().intValue() - 1;
        if (mc.player.getInventory().getStack(slot).getItem() != Items.TOTEM_OF_UNDYING)
        {
            int totemSlot = InventoryUtils.getInventoryItemSlot(Items.TOTEM_OF_UNDYING);
            if (totemSlot != -1)
            {
                if (totemSlot < 9)
                    totemSlot = totemSlot + 36;

                InventoryUtils.swap(totemSlot, slot);
                refillTimer.resetDelay();
            }
        }
    }


    @SubscribeEvent
    public void onScreenEvent(ScreenEvent.SetScreen event)
    {
        if (event.getGuiScreen() instanceof InventoryScreen)
        {
            if (openTotem.getValue())
            {
                int totemSlot = InventoryUtils.getHotbarItemSlot(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1)
                {
                    InventoryUtils.switchToSlot(totemSlot);
                }
            }
        }
    }


    @Override
    public String getDescription()
    {
        return "AntiChainPop: Mainhands a totem when you can get dtapped";
    }
}
