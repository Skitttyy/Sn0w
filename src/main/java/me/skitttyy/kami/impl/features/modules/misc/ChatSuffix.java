package me.skitttyy.kami.impl.features.modules.misc;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.mixin.accessor.IChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class ChatSuffix extends Module {

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("SN0WUNICODE")
            .register(this);

    public ChatSuffix()
    {
        super("ChatSuffix", Category.Misc);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;


        if (event.getPacket() instanceof ChatMessageC2SPacket packet)
        {
            if (allowMessage(packet.chatMessage()))
            {
                String newMessage = getText(packet.chatMessage());
                if (newMessage.length() >= 254) return;

                ((IChatMessageC2SPacket) event.getPacket()).setMessage(newMessage);
            }
        }
    }

    public String getText(String message)
    {

        if (mode.getValue().equals("SN0WUNICODE"))
        {
            return message + " " + KamiMod.NAME_UNICODE;
        } else
        {
            return message + ChatUtils.hephaestus(" | " + mode.getValue().toLowerCase());
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
    @Override
    public String getDescription()
    {
        return "ChatSuffix: append stuff after ur chat message";
    }


}
