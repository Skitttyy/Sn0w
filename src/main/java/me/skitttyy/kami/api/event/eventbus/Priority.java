package me.skitttyy.kami.api.event.eventbus;

public class Priority {
    public static final byte SUPER_FIRST = 0, MANAGER_FIRST = 1, MODULE_FIRST = 2, NORMAL = 3, MODULE_LAST = 4, MANAGER_LAST = 5;
    public static final byte[] VALUE_ARRAY = new byte[]{0, 1, 2, 3, 4, 5};
}