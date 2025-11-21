package me.skitttyy.kami.api.utils;


import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.management.FeatureManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.custom.Bind;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.regex.Pattern;

public class StringUtils
{
    private static final Pattern PATTERN_CONTROL_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");


    public static String removeLastChar(String str)
    {
        String output = "";
        if (str != null && !str.isEmpty())
        {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Nullable
    public static <T> T parseId(Registry<T> registry, String name)
    {
        name = name.toLowerCase().trim();

        Identifier id;
        if (name.contains(":")) id = Identifier.of(name);
        else id = Identifier.of("minecraft", name);
        if (registry.containsId(id)) return registry.get(id);

        return null;
    }

    public static String getKeyName(int keyCode)
    {
        if (keyCode == -1) return "NONE";

        switch (keyCode)
        {
            case 32:
                return "SPACE";
            case 39:
                return "APOSTROPHE";
            case 44:
                return "COMMA";
            case 45:
                return "MINUS";
            case 46:
                return "PERIOD";
            case 47:
                return "SLASH";
            case 48:
                return "0";
            case 49:
                return "1";
            case 50:
                return "2";
            case 51:
                return "3";
            case 52:
                return "4";
            case 53:
                return "5";
            case 54:
                return "6";
            case 55:
                return "7";
            case 56:
                return "8";
            case 57:
                return "9";
            case 59:
                return "SEMICOLON";
            case 61:
                return "EQUAL";
            case 65:
                return "A";
            case 66:
                return "B";
            case 67:
                return "C";
            case 68:
                return "D";
            case 69:
                return "E";
            case 70:
                return "F";
            case 71:
                return "G";
            case 72:
                return "H";
            case 73:
                return "I";
            case 74:
                return "J";
            case 75:
                return "K";
            case 76:
                return "L";
            case 77:
                return "M";
            case 78:
                return "N";
            case 79:
                return "O";
            case 80:
                return "P";
            case 81:
                return "Q";
            case 82:
                return "R";
            case 83:
                return "S";
            case 84:
                return "T";
            case 85:
                return "U";
            case 86:
                return "V";
            case 87:
                return "W";
            case 88:
                return "X";
            case 89:
                return "Y";
            case 90:
                return "Z";
            case 91:
                return "LEFT_BRACKET";
            case 92:
                return "BACKSLASH";
            case 93:
                return "RIGHT_BRACKET";
            case 96:
                return "GRAVE_ACCENT";
            case 161:
                return "WORLD_1";
            case 162:
                return "WORLD_2";
            case 256:
                return "ESCAPE";
            case 257:
                return "ENTER";
            case 258:
                return "TAB";
            case 259:
                return "BACKSPACE";
            case 260:
                return "INSERT";
            case 261:
                return "DELETE";
            case 262:
                return "RIGHT";
            case 263:
                return "LEFT";
            case 264:
                return "DOWN";
            case 265:
                return "UP";
            case 266:
                return "PAGE_UP";
            case 267:
                return "PAGE_DOWN";
            case 268:
                return "HOME";
            case 269:
                return "END";
            case 280:
                return "CAPS_LOCK";
            case 281:
                return "SCROLL_LOCK";
            case 282:
                return "NUM_LOCK";
            case 283:
                return "PRINT_SCREEN";
            case 284:
                return "PAUSE";
            case 290:
                return "F1";
            case 291:
                return "F2";
            case 292:
                return "F3";
            case 293:
                return "F4";
            case 294:
                return "F5";
            case 295:
                return "F6";
            case 296:
                return "F7";
            case 297:
                return "F8";
            case 298:
                return "F9";
            case 299:
                return "F10";
            case 300:
                return "F11";
            case 301:
                return "F12";
            case 302:
                return "F13";
            case 303:
                return "F14";
            case 304:
                return "F15";
            case 305:
                return "F16";
            case 306:
                return "F17";
            case 307:
                return "F18";
            case 308:
                return "F19";
            case 309:
                return "F20";
            case 310:
                return "F21";
            case 311:
                return "F22";
            case 312:
                return "F23";
            case 313:
                return "F24";
            case 314:
                return "F25";
            case 320:
                return "KP_0";
            case 321:
                return "KP_1";
            case 322:
                return "KP_2";
            case 323:
                return "KP_3";
            case 324:
                return "KP_4";
            case 325:
                return "KP_5";
            case 326:
                return "KP_6";
            case 327:
                return "KP_7";
            case 328:
                return "KP_8";
            case 329:
                return "KP_9";
            case 330:
                return "KP_DECIMAL";
            case 331:
                return "KP_DIVIDE";
            case 332:
                return "KP_MULTIPLY";
            case 333:
                return "KP_SUBTRACT";
            case 334:
                return "KP_ADD";
            case 335:
                return "KP_ENTER";
            case 336:
                return "KP_EQUAL";
            case 340:
                return "LEFT_SHIFT";
            case 341:
                return "LEFT_CONTROL";
            case 342:
                return "LEFT_ALT";
            case 343:
                return "LEFT_SUPER";
            case 344:
                return "RIGHT_SHIFT";
            case 345:
                return "RIGHT_CONTROL";
            case 346:
                return "RIGHT_ALT";
            case 347:
                return "RIGHT_SUPER";
            case 348:
                return "MENU";
            default:
                return "UNKNOWN";
        }
    }

    public static String getKeyName(int keyCode, Bind bind)
    {
        if (keyCode == -1) return "NONE";

        if (bind.getIsMouse())
        {
            return "MOUSE_" + bind.getKey();
        }
        return getKeyName(keyCode);
    }

    public static int getKeyCode(String key)
    {
        if (key.equalsIgnoreCase("NONE"))
        {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        // Keyboard Keys
        for (int i = 32; i < 97; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i)))
            {
                return i;
            }
        }
        for (int i = 256; i < 349; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i)))
            {
                return i;
            }
        }
        // Mouse Buttons
        for (int i = 1000; i < 1010; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i)))
            {
                return i;
            }
        }
        return GLFW.GLFW_KEY_UNKNOWN;
    }

    public static String getCategoryIcon(String category)
    {
        switch (category)
        {
            case "Client":
                return "❄";
            case "Combat":
                return "\uD83D\uDDE1";
            case "Movement":
                return "\uD83C\uDF2B";
            case "Player":
                return "\uD83D\uDC31";
            case "Render":
                return "\uD83D\uDC41";
            case "Misc":
                return "⚙";
            case "Ghost":
                return "\uD83D\uDC7B";
            case "Hud":
                return "\uD83D\uDCBB";
        }
        return "?";
    }





    public static String getPotionDurationString(StatusEffectInstance instance, float durationFactor)
    {
        if (instance.isInfinite())
        {
            return "**:**";
        } else
        {
            int i = MathHelper.floor((float) instance.getDuration() * durationFactor);
            return ticksToElapsedTime(i);
        }
    }


    public static String ticksToElapsedTime(int ticks)
    {
        int i = ticks / 20;
        int j = i / 60;
        i %= 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }

    public static String stripControlCodes(String text)
    {
        return PATTERN_CONTROL_CODE.matcher(text).replaceAll("");
    }


}
