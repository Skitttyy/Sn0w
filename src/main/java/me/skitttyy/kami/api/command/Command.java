package me.skitttyy.kami.api.command;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.KamiMod;

public abstract class Command implements IMinecraft {

    String name;
    String desc;
    String[] alias;

    public Command(String name, String desc, String[] alias){
        this.name = name;
        this.desc = desc;
        this.alias = alias;
        KamiMod.EVENT_BUS.register(this);
    }

    public abstract void run(String[] args);

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getAlias() {
        return alias;
    }

    public String[] getFill(String args[]){
        return new String[]{};
    }
}
