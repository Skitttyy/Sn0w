package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.misc.ClientChatEvent;
import me.skitttyy.kami.api.utils.Pair;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.impl.KamiMod;
import net.minecraft.util.Formatting;

import java.util.*;

public class CommandManager implements IMinecraft {

    public static CommandManager INSTANCE;

    public String PREFIX = "-";

    List<Command> commands = new ArrayList<>();

    public CommandManager()
    {
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event)
    {

        if (event.getMessage().startsWith(PREFIX))
        {
            event.setCancelled(true);
            //so u can do up arrow to find
            mc.inGameHud.getChatHud().addToMessageHistory(event.getMessage());

            String sub = event.getMessage().substring(1);
            String[] args = sub.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            if (args.length > 0)
            {
                Boolean hasRan = false;
                for (Command command : commands)
                {
                    for (String s : command.getAlias())
                    {
                        if (s.equalsIgnoreCase(args[0]))
                        {
                            command.run(args);
                            hasRan = true;
                            break;
                        }
                    }
                }

                if (!hasRan)
                {
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + "Invalid command! Type " + PREFIX + "help for a list of commands!",
                            false,
                            0
                    ));
                }
            } else
            {
                ChatUtils.sendMessage(new ChatMessage(
                        Formatting.RED + "Please type a command! Type " + PREFIX + "help for a list of commands!",
                        false,
                        0
                ));
            }
        }
    }

    public Pair<Command, String> findClosestMatchingCommand(String string)
    {
        for (Command command : commands)
        {
            for (String s : command.getAlias())
            {
                if (string == s) return new Pair<>(command, s);

                if (string.length() > s.length()) continue;

                if (s.startsWith(string))
                {
                    return new Pair<>(command, s);
                }
            }
        }
        return null;
    }

    public List<Command> getCommands()
    {
        return commands;
    }
}
