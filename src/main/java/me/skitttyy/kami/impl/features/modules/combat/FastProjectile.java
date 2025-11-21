package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class FastProjectile extends Module {

    public static FastProjectile INSTANCE;

    public FastProjectile()
    {
        super("FastProjectile", Category.Combat);
        INSTANCE = this;
    }


    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Type")
            .withValue("Future")
            .withModes("Future", "Stonian", "Float", "BowBomb")
            .register(this);
    Value<Number> strength = new ValueBuilder<Number>()
            .withDescriptor("Strength")
            .withValue(10)
            .withRange(1, 300)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(false)
            .register(this);
    Value<Boolean> bows = new ValueBuilder<Boolean>()
            .withDescriptor("Bows")
            .withValue(false)
            .register(this);
    Value<Boolean> pearls = new ValueBuilder<Boolean>()
            .withDescriptor("Pearls")
            .withValue(false)
            .register(this);


    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof PlayerActionC2SPacket)
        {
            PlayerActionC2SPacket packet = (PlayerActionC2SPacket) event.getPacket();

            if (packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM)
            {
                if ((mc.player.getActiveItem().getItem() == Items.BOW && bows.getValue()))
                {
                    this.sendPackets();
                }
            }
        } else if (event.getPacket() instanceof PlayerInteractItemC2SPacket)
        {

            if (((PlayerInteractItemC2SPacket) event.getPacket()).getHand() == Hand.MAIN_HAND && ((mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL && pearls.getValue())))
            {
                this.sendPackets();
            }
        }

    }

    public void sendPackets()
    {
        if (!mc.player.isSprinting())
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));


        for (int i = 0; i < strength.getValue().floatValue(); i++)
        {
            switch (mode.getValue())
            {
                //these values were packetlogged from future beta when it weas first added
                case "Future":
                    movePacket(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true);
                    movePacket(mc.player.getX(), mc.player.getY() + 10e-12, mc.player.getZ(), false);
                    break;
                //this is the like original seppeku bowbomb where u go abovea nd it does 999
                case "BowBomb":
                    movePacket(mc.player.getX(), mc.player.getY() - 0.00000000001f, mc.player.getZ(), false);
                    //dk if this onie is suppost to be false i forgot,
                    movePacket(mc.player.getX(), mc.player.getY() + 0.00000000001f, mc.player.getZ(), false);
                    break;
                //original values / konas values from stonian age
                case "Stonian":
                    movePacket(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true);
                    //dk if this onie is suppost to be false i forgot,
                    movePacket(mc.player.getX(), mc.player.getY() + 0.00000000001f, mc.player.getZ(), false);
                    break;
                //forgot tbh, just pasting this one over from sn0w cuz i think it bypassed some server cloudanarchy or something
                case "Float":
                    movePacket(mc.player.getX(), mc.player.getY() - 1e-10, mc.player.getZ(), true);
                    //dk if this onie is suppost to be false i forgot,
                    movePacket(mc.player.getX(), mc.player.getY() + 0.00000000001f, mc.player.getZ(), false);
                    break;
            }
        }

    }

    private void movePacket(double x, double y, double z, boolean ground)
    {
        if (rotate.getValue())
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
        else
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
    }


    @Override
    public String getHudInfo()
    {
        if (NullUtils.nullCheck())
            return "XD";

        if ((mc.player.getActiveItem().getItem() == Items.BOW))
            return getBowColor(mc.player.getItemUseTime()) + "" + mc.player.getItemUseTime();

        return "";
    }


    public Formatting getBowColor(double count)
    {
        if (count >= 15)
        {
            return Formatting.GREEN;
        }
        if (count >= 10)
        {
            return Formatting.DARK_GREEN;
        }
        if (count >= 5)
        {
            return Formatting.YELLOW;
        }

        return Formatting.RED;
    }

    @Override
    public String getDescription()
    {
        return "FastProjectile: An " + Formatting.RED + "exploit" + Formatting.WHITE + " that uses packets to greatly increase the speed of a projectile";
    }
}