package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.render.FrameEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.Utils32k;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.targeting.TargetUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class thirty2kaura extends Module {
    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Tick")
            .withModes("Tick", "Fps")
            .register(this);
    Value<Number> targetRange = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(15)
            .withRange(5, 50)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> switchto32k = new ValueBuilder<Boolean>()
            .withDescriptor("Switch To 32k")
            .withValue(true)
            .register(this);
    Value<Boolean> only32k = new ValueBuilder<Boolean>()
            .withDescriptor("Only 32k")
            .withValue(true)
            .register(this);
    Value<Number> waitTick = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(10)
            .withRange(0, 20)
            .register(this);
    Value<String> attackMode = new ValueBuilder<String>()
            .withDescriptor("Attack Type")
            .withValue("Client")
            .withModes("Client", "Packet")
            .register(this);
    Value<Boolean> totem32k = new ValueBuilder<Boolean>()
            .withDescriptor("Totem 32k")
            .withValue(false)
            .register(this);
    Value<Boolean> antiTotem = new ValueBuilder<Boolean>()
            .withDescriptor("Anti Totem")
            .withValue(false)
            .register(this);
    Value<Number> antiTotemTries = new ValueBuilder<Number>()
            .withDescriptor("Anti Totem Tries")
            .withValue(1)
            .withRange(1, 10)
            .withParent(antiTotem)
            .withParentEnabled(true)
            .register(this);

    public thirty2kaura()
    {
        super("32kAura", Category.Combat);
    }

    int waitCounter = 0;

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

    }

    @SubscribeEvent
    public void onUpdatePre(final TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (!mc.player.isAlive())
            return;

        if (rotate.getValue())
        {
            if (target != null)
            {
                if (rotate.getValue())
                {
                    float[] rotation = RotationUtils.getRotationsTo(mc.player.getEyePos(), KillAura.INSTANCE.getAttackRotateVec(target));
                    RotationUtils.setRotation(rotation);
                }
            }
        }

        if (mode.getValue().equals("Tick"))
        {
            doAura();
        }

    }



    Entity target;


    @SubscribeEvent
    public void onFastTick(final FrameEvent.FrameFlipEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (mode.getValue().equals("Fps"))
        {
            doAura();
        }
    }

    public void doAura()
    {
        if (mc.player.isDead())
            return;

        boolean shield = mc.player.getOffHandStack().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == Hand.OFF_HAND;
        if (mc.player.isUsingItem() && !shield)
        {
            return;
        }


        if (waitCounter < waitTick.getValue().intValue())
        {
            waitCounter++;
            return;
        } else
        {
            waitCounter = 0;
        }
        if (target != null)
        {
            attack(target, false);
        }
    }

    @SubscribeEvent
    public void onUpdate(final TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        target = TargetUtils.getTarget(targetRange.getValue().doubleValue());

        if (mode.getValue().equals("Tick"))
        {
            doAura();
        }
    }


    private void attack(Entity e, boolean forcePacket)
    {
        boolean holding32k = false;

        if (Utils32k.checkSharpness(mc.player.getMainHandStack()) && !totem32k.getValue())
        {
            holding32k = true;
        }
        int newSlot = -1;
        int totemSlot = InventoryUtils.getHotbarItemSlot(Items.TOTEM_OF_UNDYING);

        if (switchto32k.getValue() && !holding32k)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack == ItemStack.EMPTY)
                {
                    continue;
                }
                if (Utils32k.checkSharpness(stack))
                {
                    newSlot = i;
                    break;
                }
            }

            if (newSlot != -1)
            {
                if (totem32k.getValue() && totemSlot != -1)
                {
                    mc.player.getInventory().selectedSlot = totemSlot;
                } else
                {
                    mc.player.getInventory().selectedSlot = newSlot;
                }
                holding32k = true;
            }

        }

        if (only32k.getValue() && !holding32k)
        {
            return;
        }

        if (totem32k.getValue() && newSlot != -1 && totemSlot != -1)
        {
            InventoryUtils.switchToSlotGhost(newSlot);
        }
        if (attackMode.getValue().equals("Client") || !forcePacket)
        {
            if (mc.player.isSprinting() && AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
                mc.player.setSprinting(false);

            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking());
            PacketManager.INSTANCE.sendPacket(packet);
            mc.player.attack(target);
        } else
        {
            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking());
            PacketManager.INSTANCE.sendPacket(packet);
        }
        if (totem32k.getValue() && totemSlot != -1 && newSlot != -1)
        {
            InventoryUtils.switchToSlotGhost(totemSlot);
        }
        mc.player.swingHand(Hand.MAIN_HAND);

    }

    @Override
    public String getDescription()
    {
        return "32kAura: A version of KillAura that is intended for 32k/superweapons that swings at more rapid speeds (intended for impurity.me)";
    }
}
