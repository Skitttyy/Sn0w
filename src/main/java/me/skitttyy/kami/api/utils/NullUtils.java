package me.skitttyy.kami.api.utils;

import me.skitttyy.kami.api.wrapper.IMinecraft;

public class NullUtils implements IMinecraft {

    public static boolean nullCheck()
    {
        return (mc.player == null || mc.world == null || mc.interactionManager == null);
    }
}
