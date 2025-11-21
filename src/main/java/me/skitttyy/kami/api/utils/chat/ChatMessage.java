package me.skitttyy.kami.api.utils.chat;

import lombok.Getter;

@Getter
public class ChatMessage {

    int messageID;
    boolean override;
    String text;

    public ChatMessage (
            String text,
            boolean override,
            int messageID
    ) {
        this.text = text;
        this.override = override;
        this.messageID = messageID;
    }

    public boolean doesOverride() {
        return override;
    }
}
