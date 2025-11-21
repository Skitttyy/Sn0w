package me.skitttyy.kami.api.value.custom;

public class Bind {

    int key;
    public Bind(){
        this.key = -1;
        this.isMouse = false;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    boolean isMouse;

    public void setIsMouse(boolean mouse) {
       isMouse = mouse;
    }

    public boolean getIsMouse(){
        return isMouse;
    }
}
