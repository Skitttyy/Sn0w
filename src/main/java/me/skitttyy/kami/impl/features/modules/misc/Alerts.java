package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.player.PopEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.render.Nametags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;

public class Alerts extends Module
{

    public Value<Boolean> pops = new ValueBuilder<Boolean>()
            .withDescriptor("Pops")
            .withValue(true)
            .register(this);

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Color")
            .withValue("Sync")
            .withModes("Sync", "ICEHack", "Custom")
            .withParent(pops)
            .withParentEnabled(true)
            .register(this);

    Value<Sn0wColor> nameColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Name", "nameColor")
            .withValue(new Sn0wColor(255, 0, 0))
            .withPage("Custom")
            .withPageParent(mode)
            .withParent(pops)
            .withParentEnabled(true)
            .register(this);
    Value<Sn0wColor> accent = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Accent")
            .withValue(new Sn0wColor(0, 255, 0))
            .withPage("Custom")
            .withPageParent(mode)
            .withParent(pops)
            .withParentEnabled(true)
            .register(this);
    Value<Sn0wColor> popColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("PopColor")
            .withValue(new Sn0wColor(0, 0, 255))
            .withPage("Custom")
            .withPageParent(mode)
            .withParent(pops)
            .withParentEnabled(true)
            .register(this);
    public Value<Boolean> boldName = new ValueBuilder<Boolean>()
            .withDescriptor("Bold Pops")
            .withParent(pops)
            .withParentEnabled(true)
            .withValue(true)
            .register(this);
    public Value<Boolean> pearls = new ValueBuilder<Boolean>()
            .withDescriptor("Pearls")
            .withValue(true)
            .register(this);
    public Value<Boolean> boldNamePearls = new ValueBuilder<Boolean>()
            .withDescriptor("Bold Pearls")
            .withParent(pearls)
            .withParentEnabled(true)
            .withValue(true)
            .register(this);
    public Value<Boolean> strengthDetect = new ValueBuilder<Boolean>()
            .withDescriptor("Strength Detect")
            .withValue(true)
            .register(this);
    public Value<Boolean> weakDetect = new ValueBuilder<Boolean>()
            .withDescriptor("Weak Detect")
            .withValue(true)
            .register(this);
    Random rand = new Random();
    public static Alerts INSTANCE;
    Timer delay = new Timer();
    private final HashSet<PlayerEntity> list;
    private final HashSet<PlayerEntity> weaklist;

    public Alerts()
    {
        super("Alerts", Category.Misc);
        INSTANCE = this;
        delay.setDelay(5000L);

        this.list = new HashSet<PlayerEntity>();
        this.weaklist = new HashSet<PlayerEntity>();

    }

    @SubscribeEvent
    public void onTotemPop(PopEvent.TotemPopEvent event)
    {
        if (pops.getValue())
        {
            final String name = event.getEntity().getName().getString();
            int pops = event.getTotalPops();
            Entity entity = event.getEntity();
            boolean isSelf = mc.player == entity;


            switch (mode.getValue())
            {
                case "Sync":
                    if (Manager.INSTANCE.chatNotifyMode.getValue().equals("Sn0w3"))
                    {
                        ChatUtils.sendMessage(new ChatMessage((Manager.INSTANCE.chatNotifyMode.getValue().equals("DotGod") ? Formatting.DARK_AQUA + "" : Manager.INSTANCE.getMainColor()) + (boldName.getValue() ? Formatting.BOLD : "") + (isSelf ? "You" : name) + Formatting.RESET + Manager.INSTANCE.getAccent() + (isSelf ? " have" : " has") + " popped" + (pops == 0 ? "." : (isSelf ? " your " : " their ") + Manager.INSTANCE.getMainColor() + pops + MathUtil.getOrdinal(pops) + Manager.INSTANCE.getAccent() + " totem"), true, -entity.getId()));
                    } else
                    {
                        ChatUtils.sendMessage(new ChatMessage((Manager.INSTANCE.chatNotifyMode.getValue().equals("DotGod") ? Formatting.DARK_AQUA + "" : "") + (boldName.getValue() ? Formatting.BOLD : "") + (isSelf ? "You" : name) + Formatting.RESET + Manager.INSTANCE.getMainColor() + (isSelf ? " have" : " has") + " popped" + (pops == 0 ? "." : (isSelf ? " your " : " their ") + Manager.INSTANCE.getAccent() + pops + MathUtil.getOrdinal(pops) + Manager.INSTANCE.getMainColor() + " totem"), true, -entity.getId()));
                    }
                    break;
                case "ICEHack":
                    ChatUtils.sendMessage(new ChatMessage(Formatting.DARK_AQUA + "" + (boldName.getValue() ? Formatting.BOLD : "") + (isSelf ? "You" : name) + Formatting.RESET + Formatting.DARK_RED + (isSelf ? " have" : " has") + " popped" + (pops == 0 ? "." : (isSelf ? " your " : " their ") + Formatting.GOLD + pops + MathUtil.getOrdinal(pops) + " totem."), true, -entity.getId()));
                    break;
                case "Custom":

                    Text text1 = ChatUtils.withStyle((boldName.getValue() ? Formatting.BOLD : "") + (isSelf ? "You" : name) + Formatting.RESET, nameColor.getValue().getColorChatSync());

                    Text text2 = ChatUtils.withStyle((isSelf ? " have" : " has") + " popped" + (pops == 0 ? "." : (isSelf ? " your " : " their ")), accent.getValue().getColorChatSync());
                    Text text3 = ChatUtils.withStyle(pops == 0 ? "" : pops + MathUtil.getOrdinal(pops), popColor.getValue().getColorChatSync());
                    Text text4 = ChatUtils.withStyle(pops == 0 ? "" : " totem.", accent.getValue().getColorChatSync());


                    ChatUtils.sendMessage(text1.copy().append(text2).append(text3).append(text4), true, -entity.getId());


                    break;

            }
        }
    }

    @SubscribeEvent
    public void onTotemDeath(PopEvent.DeathPopEvent event)
    {
        if (pops.getValue())
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            int pops = event.getTotalPops();


            switch (mode.getValue())
            {
                case "Sync":
                    if (Manager.INSTANCE.chatNotifyMode.getValue().equals("Sn0w3"))
                    {
                        ChatUtils.sendMessage(new ChatMessage(Manager.INSTANCE.getMainColor() + (boldName.getValue() ? Formatting.BOLD : "") + player.getName().getString() + Formatting.RESET + Manager.INSTANCE.getAccent() + " died after popping" + (pops == 0 ? "." : " their " + Manager.INSTANCE.getMainColor() + pops + MathUtil.getOrdinal(pops) + Manager.INSTANCE.getAccent() + " totem"), true, -player.getId()));
                    } else
                    {
                        ChatUtils.sendMessage(new ChatMessage((Manager.INSTANCE.chatNotifyMode.getValue().equals("DotGod") ? Formatting.DARK_AQUA + "" : "") + (boldName.getValue() ? Formatting.BOLD : "") + player.getName().getString() + Formatting.RESET + Manager.INSTANCE.getMainColor() + " died after popping" + (pops == 0 ? "." : " their " + Manager.INSTANCE.getAccent() + pops + MathUtil.getOrdinal(pops) + Manager.INSTANCE.getMainColor() + " totem"), true, -player.getId()));
                    }
                    break;
                case "ICEHack":
                    ChatUtils.sendMessage(new ChatMessage(Formatting.DARK_AQUA + "" + (boldName.getValue() ? Formatting.BOLD : "") + player.getName().getString() + Formatting.RESET + Formatting.DARK_RED + " died after popping" + (pops == 0 ? "." : " their " + Formatting.GOLD + pops + MathUtil.getOrdinal(pops) + " totem."), true, -player.getId()));
                    break;
                case "Custom":
                    ChatUtils.sendMessage(new ChatMessage(Manager.INSTANCE.getMainColor() +  Manager.INSTANCE.getAccent() + " died after popping" + (pops == 0 ? "." : " their " + Manager.INSTANCE.getMainColor() + pops + MathUtil.getOrdinal(pops) + Manager.INSTANCE.getAccent() + " totem"), true, -player.getId()));

                    Text text1 = ChatUtils.withStyle((boldName.getValue() ? Formatting.BOLD : "") + player.getName().getString() + Formatting.RESET, nameColor.getValue().getColorChatSync());

                    Text text2 = ChatUtils.withStyle(" died after popping" + (pops == 0 ? "." : " their "), accent.getValue().getColorChatSync());
                    Text text3 = ChatUtils.withStyle(pops == 0 ? "" : pops + MathUtil.getOrdinal(pops), popColor.getValue().getColorChatSync());
                    Text text4 = ChatUtils.withStyle(pops == 0 ? "" : " totem.", accent.getValue().getColorChatSync());

                    ChatUtils.sendMessage(text1.copy().append(text2).append(text3).append(text4), true, -player.getId());


                    break;


            }
        }
    }


    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event)
    {
        if (pearls.getValue())
        {
            if (event.getEntity() instanceof EnderPearlEntity)
            {
                Entity player = mc.world.getClosestPlayer(event.getEntity(), 3.0);
                if (player != null)
                {
                    String facing = event.getEntity().getHorizontalFacing().toString();
                    if (facing.equals("west"))
                    {
                        facing = "east";
                    } else if (facing.equals("east"))
                    {
                        facing = "west";
                    }
                    ChatUtils.sendMessage(new ChatMessage("" + (FriendManager.INSTANCE.isFriend(player) ? Manager.INSTANCE.getMainColor() : "") + (boldNamePearls.getValue() ? Formatting.BOLD : "") + player.getName().getString() + Formatting.WHITE + " has thrown an ender pearl " + Manager.INSTANCE.getMainColor() + facing + "!", false, 27312));
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e)
    {
        if (NullUtils.nullCheck()) return;


        if (strengthDetect.getValue() || weakDetect.getValue())
        {
            for (PlayerEntity player : mc.world.getPlayers())
            {
                boolean isSelf = mc.player == player;

                if (strengthDetect.getValue())
                {
                    if (player.hasStatusEffect(StatusEffects.STRENGTH) && !list.contains(player))
                    {
                        ChatUtils.sendMessage(new ChatMessage(((FriendManager.INSTANCE.isFriend(player) || isSelf) ? Manager.INSTANCE.getMainColor() : Formatting.DARK_AQUA) + (isSelf ? "You" : player.getName().getString()) + Formatting.GREEN + " now " + (isSelf ? "have " : "has ") + "Strength!", false, 27312));
                        list.add(player);
                    }
                    if (!player.hasStatusEffect(StatusEffects.STRENGTH) && list.contains(player))
                    {
                        ChatUtils.sendMessage(new ChatMessage(((FriendManager.INSTANCE.isFriend(player) || isSelf) ? Manager.INSTANCE.getMainColor() : Formatting.DARK_AQUA) + (isSelf ? "You" : player.getName().getString()) + Formatting.DARK_RED + " " + (isSelf ? "have " : "has ") + "lost " + (isSelf ? "your " : "their ") + "Strength!", false, 27312));
                        list.remove(player);
                    }
                }
                if (weakDetect.getValue())
                {
                    if (player.hasStatusEffect(StatusEffects.WEAKNESS) && !weaklist.contains(player))
                    {
                        ChatUtils.sendMessage(new ChatMessage(((FriendManager.INSTANCE.isFriend(player) || isSelf) ? Manager.INSTANCE.getMainColor() : Formatting.DARK_AQUA) + (isSelf ? "You" : player.getName().getString()) + Formatting.WHITE + " " + (isSelf ? "have " : "has ") + "got Weakness!", false, 27312));
                        weaklist.add(player);
                    }
                    if (!player.hasStatusEffect(StatusEffects.WEAKNESS) && weaklist.contains(player))
                    {
                        ChatUtils.sendMessage(new ChatMessage(((FriendManager.INSTANCE.isFriend(player) || isSelf) ? Manager.INSTANCE.getMainColor() : Formatting.DARK_AQUA) + (isSelf ? "You" : player.getName().getString()) + Formatting.WHITE + " " + (isSelf ? "have " : "has ") + "lost " + (isSelf ? "your " : "their ") + "" + Formatting.DARK_GRAY + "Weakness" + Formatting.WHITE + "!", false, 27312));
                        weaklist.remove(player);
                    }
                }
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Alerts: Alerts you of various pvp related things";
    }

}
