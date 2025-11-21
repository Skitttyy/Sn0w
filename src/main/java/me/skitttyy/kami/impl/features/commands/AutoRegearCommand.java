package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.management.SavableManager;
import net.minecraft.item.ItemStack;
import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import net.minecraft.util.Formatting;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class AutoRegearCommand extends Command {

    public AutoRegearCommand() {
        super("AutoRegear", "lets u save kits", new String[]{"autoregear"});
    }

    public static final File pathSave = new File(System.getProperty("user.dir") + File.separator + "Sn0w" + File.separator + "misc" + File.separator + "kits.yml");

    @Override
    public void run(String[] args) {
        if(!(args.length >= 1)) return;

        switch (args[1].toLowerCase()) {
            case "list":
                if (args.length == 2) {
                    sendList();
                } else {
                    errorMessage("cope");
                }
                break;
            case "set":
                if (args.length == 3) {
                    set(args[2]);
                } else {
                    errorMessage(Formatting.RED + "Invalid syntax! Syntax is: regear set/create/list/del kit");
                }
                break;
            case "save":
            case "create":
                if (args.length == 3) {
                    save(args[2]);
                } else {
                    errorMessage(Formatting.RED + "Invalid syntax! Syntax is: regear set/create/list/del kit");
                }
                break;
            case "del":
                if (args.length == 3) {
                    delete(args[2]);
                } else errorMessage(Formatting.RED + "Invalid syntax! Syntax is: regear set/create/list/del kit");
                break;
            case "":
            case "help":
            default:
                errorMessage(Formatting.RED + "Invalid syntax! Syntax is: regear set/create/list/del kit");
                break;
        }
    }

    private void sendList() {
        try {
            // Read json

            Map<String, Object> map = SavableManager.INSTANCE.yaml.load(new FileReader(pathSave));
            int lengt = map.entrySet().size();
            for (int i = 0; i < lengt; i++) {
                String item = map.entrySet().stream().toList().get(i).getKey();
                if (!item.equals("pointer"))
                    ChatUtils.sendMessage(new ChatMessage("Kit: " + item, false, 1777));
            }

        } catch (IOException e) {
        }
    }

    private void set(String name) {
        try {
            // Read json

            Map<String, Object> map = SavableManager.INSTANCE.yaml.load(new FileReader(pathSave));
            if (map.get(name) != null && !name.equals("pointer")) {
                // Change the value
                map.put("pointer", name);
                // Save
                saveFile(map, name, "selected");
            } else errorMessage("NoEx");

        } catch (IOException e) {
            // Case not found, reset
            errorMessage("NoEx");
        }
    }

    private void delete(String name) {
        try {
            // Read json

            InputStream inputStream = new FileInputStream(pathSave);

            Map<String, Object> map = SavableManager.INSTANCE.yaml.load(inputStream);

            if (map.get(name) != null && !name.equals("pointer")) {
                // Delete
                map.remove(name);
                // Check if it's setter
                if (map.get("pointer").toString().equals(name))
                    map.put("pointer", "none");
                // Save
                saveFile(map, name, "has been deleted!");
            } else {

            }

        } catch (IOException e) {
            // Case not found, reset
        }
    }

    public static void save(String name) {
        Map<String, Object> map = new HashMap<>();
        try {
            // Read json
            map = SavableManager.INSTANCE.yaml.load(new FileReader(pathSave));
            if (map.get(name) != null && !name.equals("pointer")) {
                errorMessage(Formatting.BLUE + "Kit " + Formatting.AQUA + name + Formatting.BLUE + " already exists!");
                return;
            }
            // We can continue

        } catch (IOException e) {
            // Case not found, reset
            map.put("pointer", "none");
        }

        // String that is going to be our inventory
        StringBuilder jsonInventory = new StringBuilder();
        for (ItemStack item : mc.player.getInventory().main) {
            // Add everything
            jsonInventory.append(item.getItem().getDefaultStack().getTranslationKey()).append(" ");
        }
        // Add to the json
        map.put(name, jsonInventory.toString());
        // Save json
        saveFile(map, name, "has been saved!");
    }

    public static void errorMessage(String error) {
        ChatUtils.sendMessage(new ChatMessage(error, false, 1777));
    }

    public static void sendMessage(String message) {
        ChatUtils.sendMessage(new ChatMessage(Formatting.RED + message, false, 1777));
    }

    public static void saveFile(Map<String, Object> map, String name, String operation) {
        // Save the json
        try {
            // Open

            SavableManager.INSTANCE.yaml.dump(map, new FileWriter(pathSave));
            sendMessage(Formatting.AQUA + name + " " + Formatting.BLUE + operation);
        } catch (IOException e) {
            errorMessage(Formatting.RED + "Error saving kit :( please try again!");
        }
    }

    public static String getCurrentSet() {
        try {
            // Read json
            Map<String, Object> map = SavableManager.INSTANCE.yaml.load(new FileReader(pathSave));
            if (!map.get("pointer").equals("none"))
                return map.get("pointer").toString();


        } catch (IOException e) {
            // Case not found, reset
        }
//        errorMessage("NoEx");
        return "";
    }

    public static String getInventoryKit(String kit) {
        try {
            // Read json
            Map<String, Object> map = SavableManager.INSTANCE.yaml.load(new FileReader(pathSave));
            return map.get(kit).toString();


        } catch (IOException e) {
            // Cas
            // e not found, reset
        }
//        errorMessage("NoEx");
        return "";
    }


    @Override
    public String[] getFill(String[] args)
    {

        return new String[]{"{set,create,del,help}", "name"};
    }

}
