package me.skitttyy.kami.api.utils.math;

import me.skitttyy.kami.api.wrapper.IMinecraft;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HexRandom implements IMinecraft
{
    private static final String HEX_CHARS = "0123456789abcdef";
    static Random RANDOM = ThreadLocalRandom.current();

    public static String generateRandomHex(int length)
    {
        StringBuilder hexString = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            int index = RANDOM.nextInt(HEX_CHARS.length());
            hexString.append(HEX_CHARS.charAt(index));
        }
        return hexString.toString();
    }
}