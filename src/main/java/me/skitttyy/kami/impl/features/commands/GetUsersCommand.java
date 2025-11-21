package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.Sn0wUser;

public class GetUsersCommand extends Command {
    public GetUsersCommand()
    {
        super("GetUsers", "shows all the users on sn0w", new String[]{"getusers", "users"});
    }

    @Override
    public void run(String[] args)
    {
//        ChatUtils.sendMessage(new ChatMessage(
//               "There is currently " + BotManager.INSTANCE.sn0wUserArrayList.size() + " user(s) online!",
//                false,
//                0
//        ));
//        for (Sn0wUser user : BotManager.INSTANCE.sn0wUserArrayList)
//        {
//            ChatUtils.sendMessage(new ChatMessage(
//                    user.getName(),
//                    false,
//                    0
//            ));
//        }
    }
}
