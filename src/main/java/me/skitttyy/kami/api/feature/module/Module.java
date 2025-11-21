package me.skitttyy.kami.api.feature.module;

import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.BindManager;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.impl.KamiMod;

import me.skitttyy.kami.api.binds.IBindable;
import me.skitttyy.kami.api.feature.Feature;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.api.value.custom.Bind;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.Manager;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Map;

public class Module extends Feature implements IBindable, IMinecraft
{


    Bind bind;
    Value<Boolean> chatNotify = new ValueBuilder<Boolean>()
            .withDescriptor("Chat Notify")
            .withValue(true)
            .register(this);


    public Module(String name, Category category)
    {
        super(name, category, FeatureType.Module);
        this.bind = new Bind();
        BindManager.INSTANCE.getBindables().add(this);
    }

    public Value<?> register(Value<?> value)
    {
        getValues().add(value);
        return value;
    }

    public Bind getBind()
    {
        return bind;
    }

    public void setBind(Bind bind)
    {
        this.bind = bind;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.world != null)
        {
            offset = Math.negateExact(mc.textRenderer.getWidth(this.getName() + " [" + this.getHudInfo() + "]"));
        }
//        if (Manager.INSTANCE.moduleNotifications.getValue())
//        {
//            KamiMod.notificationProcessor.addNotification(Formatting.RESET + getDisplayName() + Formatting.WHITE + " was " + Formatting.GREEN + "enabled" + Formatting.WHITE + ".", 500L);
//        }
        if (chatNotify.getValue())
        {
            switch (Manager.INSTANCE.chatNotifyMode.getValue())
            {
                case "Sn0w":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Manager.INSTANCE.getMainColor() + "enabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "Sn0w2":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Formatting.GREEN + "enabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "Sn0w3":
                    ChatUtils.sendMessage(new ChatMessage(
                            Manager.INSTANCE.getMainColor() + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Manager.INSTANCE.getAccent() + " was " + Formatting.GREEN + "enabled" + Manager.INSTANCE.getAccent() + ".",
                            true,
                            69420
                    ));
                    break;
                case "DotGod":
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.DARK_AQUA + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.LIGHT_PURPLE + " was " + Formatting.GREEN + "enabled" + Formatting.LIGHT_PURPLE + ".",
                            true,
                            69420
                    ));
                    break;
                case "NoPrefix":
                    ChatUtils.sendMessageNoPrefix(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Manager.INSTANCE.getMainColor() + "enabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "ForgeHax":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Manager.INSTANCE.getMainColor() + "." + Manager.INSTANCE.getAccent() + "enabled " + Manager.INSTANCE.getMainColor() + "= " + Formatting.GREEN + "true.",
                            true,
                            69420
                    ));
                    break;
                case "ForgeHaxReal":
                    ChatUtils.sendMessageNoPrefix(new ChatMessage(
                            ">" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + ".enabled = true",
                            true,
                            69420
                    ));
                    break;
                case "Aurora":
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.GREEN + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.GREEN + " Enabled",
                            true,
                            69420
                    ));
                    break;

            }

        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (chatNotify.getValue())
        {
            switch (Manager.INSTANCE.chatNotifyMode.getValue())
            {
                case "Sn0w":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Manager.INSTANCE.getAccent() + "disabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "Sn0w2":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Formatting.RED + "disabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "Sn0w3":
                    ChatUtils.sendMessage(new ChatMessage(
                            Manager.INSTANCE.getMainColor() + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Manager.INSTANCE.getAccent() + " was " + Formatting.RED + "disabled" + Manager.INSTANCE.getAccent() + ".",
                            true,
                            69420
                    ));
                    break;
                case "DotGod":
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.DARK_AQUA + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.LIGHT_PURPLE + " was " + Formatting.RED + "disabled" + Formatting.LIGHT_PURPLE + ".",
                            true,
                            69420
                    ));
                    break;
                case "NoPrefix":
                    ChatUtils.sendMessageNoPrefix(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.WHITE + " was " + Manager.INSTANCE.getAccent() + "disabled" + Formatting.WHITE + ".",
                            true,
                            69420
                    ));
                    break;
                case "ForgeHax":
                    ChatUtils.sendMessage(new ChatMessage(
                            (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Manager.INSTANCE.getMainColor() + "." + Manager.INSTANCE.getAccent() + "enabled " + Manager.INSTANCE.getMainColor() + "= " + Formatting.RED + "false.",
                            true,
                            69420
                    ));
                    break;
                case "ForgeHaxReal":
                    ChatUtils.sendMessageNoPrefix(new ChatMessage(
                            ">" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + ".enabled = false",
                            true,
                            69420
                    ));
                    break;
                case "Aurora":
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + "" + (Manager.INSTANCE.bold.getValue() ? Formatting.BOLD : "") + getDisplayName() + Formatting.RESET + Formatting.RED + " Disabled",
                            true,
                            69420
                    ));
                    break;

            }

        }
    }

    @Override
    public void load(Map<String, Object> objects)
    {
        super.load(objects);

        if ((objects.get("bind").toString()).contains("MOUSE_"))
        {
            bind.setIsMouse(true);
            String bindString = objects.get("bind").toString();
            bind.setKey(Integer.parseInt(bindString.replace("MOUSE_", "")));

        } else
        {
            bind.setKey(((int) objects.get("bind")));
        }
    }

    @Override
    public Map<String, Object> save()
    {
        Map<String, Object> toSave = super.save();
        if ((bind.getIsMouse()))
        {
            toSave.put("bind", "MOUSE_" + bind.getKey());

        } else
        {
            toSave.put("bind", bind.getKey());
        }
        return toSave;
    }

    @Override
    public void loadModule(Map<String, Object> objects)
    {
        super.loadModule(objects);
        if ((objects.get("bind").toString()).contains("MOUSE_"))
        {
            bind.setIsMouse(true);
            String bindString = objects.get("bind").toString();
            bind.setKey(Integer.parseInt(bindString.replace("MOUSE_", "")));

        } else
        {
            bind.setKey(((int) objects.get("bind")));
        }
    }

    @Override
    public int getKey()
    {
        return bind.getKey();
    }

    @Override
    public void onKey()
    {
        if (!bind.getIsMouse())
            this.toggle();
    }

}
