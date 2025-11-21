package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.Rotation;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

public class AutoXP extends Module {
    public static AutoXP INSTANCE;
    public Value<Number> xpSpeed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(20)
            .withRange(0.1, 20)
            .withPlaces(1)
            .register(this);
    public Value<Number> boost = new ValueBuilder<Number>()
            .withDescriptor("Boost")
            .withValue(0)
            .withRange(0, 5)
            .withPlaces(0)
            .register(this);
    Value<String> rotate = new ValueBuilder<String>()
            .withDescriptor("Rotate")
            .withValue("None")
            .withModes("None", "Packet", "Both")
            .register(this);
    public Value<Boolean> middleClick = new ValueBuilder<Boolean>()
            .withDescriptor("Middle Click")
            .withValue(true)
            .register(this);
    public Value<Boolean> mend = new ValueBuilder<Boolean>()
            .withDescriptor("Mend")
            .withValue(true)
            .register(this);
    Timer speed = new Timer();

    public AutoXP()
    {
        super("AutoXP", Category.Combat);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTickPre(TickEvent.PlayerTickEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;

        speed.setDelayCPS(xpSpeed.getValue().floatValue());


        if (!canXp()) return;

        if (rotate.getValue().equals("Both"))
            RotationManager.INSTANCE.rotateTo(new Rotation(mc.player.getYaw(), 90));

        int slot = InventoryUtils.getHotbarItemSlot(Items.EXPERIENCE_BOTTLE);


        boolean swap = slot != mc.player.getInventory().selectedSlot;

        int oldSlot = mc.player.getInventory().selectedSlot;
        if (swap)
            InventoryUtils.switchToSlot(slot);


        for (int i = 0; i < 1 + boost.getValue().intValue(); i++)
        {
            PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), !rotate.getValue().equals("None") ? 90 : mc.player.getPitch()));
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (swap)
            InventoryUtils.switchToSlot( oldSlot);

        speed.resetDelay();
    }

    public boolean canXp()
    {
        if (!speed.isPassed()) return false;


        return isXping();
    }

    public boolean isXping()
    {
        if (middleClick.getValue())
        {
            if (!mc.mouse.wasMiddleButtonClicked())
                return false;

            HitResult mouseOver = mc.crosshairTarget;

            if (mouseOver == null || mouseOver.getType() != HitResult.Type.BLOCK)
                return false;


        }


        int slot = InventoryUtils.getHotbarItemSlot(Items.EXPERIENCE_BOTTLE);

        if (slot == -1) return false;


        if (mend.getValue())
            for (ItemStack stack : mc.player.getInventory().armor)
            {
                if (stack == ItemStack.EMPTY) continue;


                String armor = InventoryUtils.getArmorPieceName(stack);
                if (armor == null) continue;


                float green = ((float) stack.getMaxDamage() - (float) stack.getDamage()) / (float) stack.getMaxDamage();
                float red = 1 - green;
                int dmg = 100 - (int) (red * 100);
                if (dmg <= 98)
                {
                    return true;
                }
            }


        return !mend.getValue();
    }

    @SubscribeEvent
    public void onTickPost(TickEvent.MovementTickEvent.Post event)
    {

        if (NullUtils.nullCheck()) return;

        if (!canXp()) return;


    }

    @Override
    public String getHudInfo()
    {
        if (NullUtils.nullCheck())
            return "";

        return InventoryUtils.getItemCount(Items.EXPERIENCE_BOTTLE) + "";
    }

    @Override
    public String getDescription()
    {
        return "AutoXP: lose the gearplay with this module";
    }
}
