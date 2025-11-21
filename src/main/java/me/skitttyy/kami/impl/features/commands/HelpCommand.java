package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.CommandManager;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", "shows you all the commands", new String[]{"help", "commands"});
    }

    @Override
    public void run(String[] args) {
        for (Command command : CommandManager.INSTANCE.getCommands()){
            ChatUtils.sendMessage(new ChatMessage(
                    command.getName() + " - " + command.getDesc(),
                    false,
                    0
            ));
        }
    }
}
