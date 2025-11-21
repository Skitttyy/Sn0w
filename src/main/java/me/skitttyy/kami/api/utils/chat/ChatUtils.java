package me.skitttyy.kami.api.utils.chat;

import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.ducks.IChatHud;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import me.skitttyy.kami.impl.features.modules.render.Nametags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;

public class ChatUtils implements IMinecraft
{

    public static String CLIENT_NAME = Formatting.BLUE + "[" + Formatting.AQUA + KamiMod.NAME + Formatting.BLUE + "]" + Formatting.RESET;

    public static void sendMessage(ChatMessage message)
    {
        if (NullUtils.nullCheck() || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;


        CLIENT_NAME = Manager.INSTANCE.getAccent() + "[" + Manager.INSTANCE.getMainColor() + KamiMod.NAME + Manager.INSTANCE.getAccent() + "]" + Formatting.RESET;

        if (message.doesOverride())
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageWithId(Text.of(CLIENT_NAME + " " + message.getText()), message.getMessageID());
        } else
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageNoId(Text.of(CLIENT_NAME + " " + message.getText()));
        }
    }

    public static void sendMessageNoPrefix(ChatMessage message)
    {
        if (NullUtils.nullCheck() || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;

        CLIENT_NAME = Manager.INSTANCE.getAccent() + "[" + Manager.INSTANCE.getMainColor() + KamiMod.NAME + Manager.INSTANCE.getAccent() + "]" + Formatting.RESET;


        if (message.doesOverride())
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageWithId(Text.of(message.getText()), message.getMessageID());
        } else
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageNoId(Text.of(message.getText()));
        }
    }

    public static void sendMessage(String message)
    {
        if (NullUtils.nullCheck() || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;

        CLIENT_NAME = Manager.INSTANCE.getAccent() + "[" + Manager.INSTANCE.getMainColor() + KamiMod.NAME + Manager.INSTANCE.getAccent() + "]" + Formatting.RESET;


        ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageNoId(Text.of(CLIENT_NAME + " " + message));
    }


    public static void sendMessage(Text message, boolean override, int id)
    {
        if (NullUtils.nullCheck() || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;


        CLIENT_NAME = Manager.INSTANCE.getAccent() + "[" + Manager.INSTANCE.getMainColor() + KamiMod.NAME + Manager.INSTANCE.getAccent() + "]" + Formatting.RESET;

        if (override)
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageWithId(Text.of(CLIENT_NAME + " ").copy().append(message), id);
        } else
        {
            ((IChatHud) mc.inGameHud.getChatHud()).addChatMessageNoId(Text.of(CLIENT_NAME + " ").copy().append(message));
        }
    }


    public static void sendChatMessage(String message)
    {
        if (message.length() >= 254) return;


        mc.inGameHud.getChatHud().addToMessageHistory(message);

        if (message.startsWith("/")) mc.player.networkHandler.sendChatCommand(message.substring(1));
        else mc.player.networkHandler.sendChatMessage(message);
    }

    public static String hephaestus(String string)
    {
        String str = string;

        str = str.replace("a", "\u1d00");
        str = str.replace("b", "\u0299");
        str = str.replace("c", "\u1d04");
        str = str.replace("d", "\u1d05");
        str = str.replace("e", "\u1d07");
        str = str.replace("f", "\u0493");
        str = str.replace("g", "\u0262");
        str = str.replace("h", "\u029c");
        str = str.replace("i", "\u026a");
        str = str.replace("j", "\u1d0a");
        str = str.replace("k", "\u1d0b");
        str = str.replace("l", "\u029f");
        str = str.replace("m", "\u1d0d");
        str = str.replace("n", "\u0274");
        str = str.replace("o", "\u1d0f");
        str = str.replace("p", "\u1d18");
        str = str.replace("q", "\u01eb");
        str = str.replace("r", "\u0280");
        str = str.replace("s", "\u0455");
        str = str.replace("t", "\u1d1b");
        str = str.replace("u", "\u1d1c");
        str = str.replace("v", "\u1d20");
        str = str.replace("w", "\u1d21");
        str = str.replace("x", "\u0445");
        str = str.replace("y", "\u028f");
        str = str.replace("z", "\u1d22");

        // The |.
        str = str.replace("|", "\u23D0");

        return str;
    }


    public static Text withStyle(String message, Color color)
    {
        Text text = Text.of(message);

        return text.copy().setStyle(text.getStyle().withColor(color.getRGB()));
    }
}
