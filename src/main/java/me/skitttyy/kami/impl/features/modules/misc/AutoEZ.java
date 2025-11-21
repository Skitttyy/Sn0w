package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.LivingEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.management.PopManager;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.world.PacketUtils;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.*;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class AutoEZ extends Module {

    public final ConcurrentHashMap<AttackedPlayer, Long> attackedPlayers;
    public Value<String> Mode = new ValueBuilder<String>()
            .withDescriptor("Mode", "autoEzmMODE")
            .withValue("Sn0w")
            .withModes("Sn0w", "Guinness Platinum", "Future Beta", "1111DotFun", "HyperLethal")
            .register(this);
    public static AutoEZ INSTANCE;

    public AutoEZ()
    {
        super("AutoEZ", Category.Misc);
        this.attackedPlayers = new ConcurrentHashMap<AttackedPlayer, Long>();
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet)
        {
            if (PacketUtils.getInteractType(packet) != PacketUtils.InteractType.ATTACK) return;

            Entity entity = PacketUtils.getEntity(packet);

            if (entity == null || !(entity instanceof PlayerEntity)) return;

            AttackedPlayer player = null;
            for (final Map.Entry<AttackedPlayer, Long> attackedPlayer : attackedPlayers.entrySet())
            {
                if ((attackedPlayer.getKey()).player.equals(entity))
                {
                    player = attackedPlayer.getKey();
                    break;
                }
            }
            final AttackedPlayer updated = new AttackedPlayer((PlayerEntity) entity);
            if (player != null)
            {
                final int attacks = player.attackPackets;
                updated.attackPackets = attacks + 1;
                attackedPlayers.remove(player);
            }
            attackedPlayers.put(updated, System.currentTimeMillis());
        }
    }

    @SubscribeEvent
    public void onDeathEvent(LivingEvent.Death event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof PlayerEntity)) return;

        for (final Map.Entry<AttackedPlayer, Long> attackedPlayer : attackedPlayers.entrySet())
        {
            if (attackedPlayer.getKey().player.equals(entity))
            {
                doAutoEZ(attackedPlayer.getKey());
                return;
            }
        }

    }

    private void doAutoEZ(final AttackedPlayer attackedPlayer)
    {
        final String name = attackedPlayer.player.getName().getString();
        final int attacks = attackedPlayer.attackPackets;
        final int pops = PopManager.INSTANCE.getPops(attackedPlayer.player);
        final int fakePops = (pops > 0) ? ((pops <= 4) ? (pops + 2) : ((int) Math.pow(pops, 2.0) / 4)) : 0;
        final Random random = new Random();
        switch (random.nextInt(8))
        {
            case 0:
            {
                ChatUtils.sendChatMessage(String.format("> " + Mode.getValue() + " (version zeta-epsilon+) just distributed %s attack packets onto %s, killing him instantly.", attacks, name));
                break;
            }
            case 1:
            {
                ChatUtils.sendChatMessage(String.format("> %s suffered %s packets from this " + Mode.getValue() + " (zeta-epsilon+ edition).", name, attacks));
                break;
            }
            case 2:
            {
                ChatUtils.sendChatMessage(String.format("> " + Mode.getValue() + " just expelled %s attack packets straight to %s using this %s.", attacks, name, mc.player.getMainHandStack().getName().getString()));
                break;
            }
            case 3:
            {
                if (pops > 0)
                {
                    ChatUtils.sendChatMessage(String.format("> %s died after receiving %s packets from this " + Mode.getValue() + ", popping %s totems in the process.", name, attacks, fakePops));
                    break;
                }
            }
            case 4:
            {
                if (pops > 0)
                {
                    ChatUtils.sendChatMessage(String.format("> %s lost %s totems courtesy of this " + Mode.getValue() + " (v zeta-epsilon+) (beta)!", name, fakePops));
                    break;
                }
            }
            case 5:
            {
                ChatUtils.sendChatMessage(String.format("> %s (betaless) was generously gifted %s attack packets, courtesy of this " + Mode.getValue() + " (beta)!", name, attacks));
                break;
            }
            case 6:
            {
                ChatUtils.sendChatMessage(String.format("> Vladimir Putin awarded me with the award I am now imperator I I ordered %s packets to %s via " + Mode.getValue(), attacks, name));
                break;
            }
            case 7:
            {
                ChatUtils.sendChatMessage(String.format("> %s weak dog against the KING it came to me to send %s attack packets and they will not return home " + Mode.getValue(), name, attacks));
                break;
            }
            case 8:
            {
                ChatUtils.sendChatMessage(String.format("> %s got sent the poor farm thanks to " + Mode.getValue(), name));
                break;
            }
        }
    }

    boolean allowMessage(String message)
    {
        boolean allow = true;

        for (String s : filters)
        {
            if (message.startsWith(s))
            {
                allow = false;
                break;
            }
        }

        return allow;
    }

    String[] filters = new String[]{
            ".",
            "/",
            ",",
            ":",
            "`",
            "-"
    };

    public static final class AttackedPlayer {
        private final PlayerEntity player;
        private int attackPackets;

        private AttackedPlayer(final PlayerEntity player)
        {
            this.player = player;
            this.attackPackets = 0;
        }
    }

    @Override
    public String getDescription()
    {
        return "AutoEZ: Sends a message in chat when you kill someone";
    }
}
