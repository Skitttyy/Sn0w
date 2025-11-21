package me.skitttyy.kami.api.utils.ducks;

import net.minecraft.text.Text;

public interface IChatHud {
    void addChatMessageWithId(Text message, int id);

    void addChatMessageNoId(Text message);


}