package me.skitttyy.kami.impl.features.modules.player;

import com.google.common.collect.Sets;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

public class AntiCrash extends Module {

    public AntiCrash()
    {
        super("AntiCrash", Category.Player);
    }


    Value<Boolean> chat = new ValueBuilder<Boolean>()
            .withDescriptor("No Unicode")
            .withValue(false)
            .register(this);
    Value<Boolean> particles = new ValueBuilder<Boolean>()
            .withDescriptor("Particles")
            .withValue(false)
            .register(this);
    Value<Boolean> sound = new ValueBuilder<Boolean>()
            .withDescriptor("Sound")
            .withValue(false)
            .register(this);


    private final static Set<SoundEvent> LAG_SOUNDS = Set.of(
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value()
    );


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;


        if (chat.getValue())
        {
            if (event.getPacket() instanceof ChatMessageS2CPacket packet)
            {
                String text = packet.body().content().toString();

                int flag = 0;
                for (char currentChar : text.toCharArray())
                {
                    if (Character.UnicodeBlock.of(currentChar) != Character.UnicodeBlock.BASIC_LATIN) flag++;
                }

                if (flag > 20)
                {

                    Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text) {
                        @Override
                        public Action getAction()
                        {
                            return Action.SUGGEST_COMMAND;
                        }
                    });
                    mc.inGameHud.getChatHud().addMessage(Text.literal(Manager.INSTANCE.getAccent() + "[" + Manager.INSTANCE.getMainColor() + KamiMod.NAME_UNICODE + Manager.INSTANCE.getAccent() + "]" + Formatting.RESET + " Blocked a chat message with " + flag + " flags." + Manager.INSTANCE.getAccent()+ " [" + Manager.INSTANCE.getMainColor() + "Click Me To View!" + Manager.INSTANCE.getAccent() + "]").setStyle(style));
                    event.setCancelled(true);
                }
            }
        }
        if (particles.getValue())
        {
            if (event.getPacket() instanceof ParticleS2CPacket packet)
            {
                if (packet.getCount() > 800)
                {
                    ChatUtils.sendMessage(new ChatMessage("A server administrator attempted to crash your game! Method: Particles", true, 13352));
                    event.setCancelled(true);
                }
            }
        }
        if (sound.getValue())
        {
            if (event.getPacket() instanceof PlaySoundFromEntityS2CPacket packet
                    && LAG_SOUNDS.contains(packet.getSound().value())
                    || event.getPacket() instanceof PlaySoundS2CPacket packet2
                    && LAG_SOUNDS.contains(packet2.getSound().value()))
            {
                event.setCancelled(true);
            }
        }

    }

    @Override
    public String getDescription()
    {
        return "AntiCrash: prevents various crashes";
    }

}