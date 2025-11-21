package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.SavableManager;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import net.minecraft.util.Formatting;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("Config", "loads and saves configs", new String[]{"config"});
    }

    @Override
    public void run(String[] args) {
        if (args.length > 2) {
            boolean error = false;
            if (args[1].equalsIgnoreCase("save")) {
                try {
                    SavableManager.INSTANCE.saveModuleConfig(args[2]);
                } catch (Exception e) {
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + "Failed to save config " + args[2] + ".yml!",
                            false,
                            0
                    ));
                    error = true;
                } finally {
                    if (!error)
                        ChatUtils.sendMessage(new ChatMessage(
                                Formatting.BLUE + "Successfully saved config " + args[2] + ".yml!",
                                false,
                                0
                        ));
                }
            } else if (args[1].equalsIgnoreCase("load")) {
                try {
                    SavableManager.INSTANCE.loadModuleConfig(args[2]);
                } catch (Exception e) {
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + e.getMessage(),
                            false,
                            0
                    ));
                    error = true;
                } finally {
                    if (!error)
                        ChatUtils.sendMessage(new ChatMessage(
                                Formatting.BLUE + "Successfully loaded config " + args[2] + ".yml!",
                                false,
                                0
                        ));
                }
            } else if (args[1].equalsIgnoreCase("softload")) {
                try {
                    SavableManager.INSTANCE.softLoadConfig(args[2]);
                } catch (Exception e) {
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + e.getMessage(),
                            false,
                            0
                    ));
                    error = true;
                } finally {
                    if (!error)
                        ChatUtils.sendMessage(new ChatMessage(
                                Formatting.BLUE + "Successfully soft-loaded config " + args[2] + ".yml!",
                                false,
                                0
                        ));
                }
            } else if (args[1].equalsIgnoreCase("del") ||args[1].equalsIgnoreCase("delete") ) {
                try {
                    SavableManager.INSTANCE.deleteModuleConfig(args[2]);
                } catch (Exception e) {
                    ChatUtils.sendMessage(new ChatMessage(
                            Formatting.RED + e.getMessage(),
                            false,
                            0
                    ));
                    error = true;
                } finally {
                    if (!error)
                        ChatUtils.sendMessage(new ChatMessage(
                                Formatting.BLUE + "Successfully deleted config " + args[2] + ".yml!",
                                false,
                                0
                        ));
                }
            } else{
                ChatUtils.sendMessage(new ChatMessage(
                        Formatting.RED + "Bad usage! config <save/del/load/softload/list/folder> <name>",
                        false,
                        0
                ));
            }
        } else if(args.length > 1){
            if (args[1].equalsIgnoreCase("list") ) {
                try {
                    SavableManager.INSTANCE.listConfigs();
                } catch (Exception e){
                }
            } else if (args[1].equalsIgnoreCase("folder") || args[1].equalsIgnoreCase("open")) {
                try {
                    SavableManager.INSTANCE.openFolder();
                } catch (Exception e){
                }
            }else{
                ChatUtils.sendMessage(new ChatMessage(
                        Formatting.RED + "Bad usage! config <save/del/load/list/folder> <name>",
                        false,
                        0
                ));
            }
        }else {
            ChatUtils.sendMessage(new ChatMessage(
                    "Invalid format",
                    false,
                    0
            ));
        }
    }

    @Override
    public String[] getFill(String[] args)
    {
        return new String[]{"{folder,del,save,load,softload}", "{CONFIGFILE}"};
    }

}
