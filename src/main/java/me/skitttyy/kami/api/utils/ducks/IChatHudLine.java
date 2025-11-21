package me.skitttyy.kami.api.utils.ducks;


public interface IChatHudLine {
    String getMessageText();

    int getOverrideId();

    void setOverrideId(int id);
    
}