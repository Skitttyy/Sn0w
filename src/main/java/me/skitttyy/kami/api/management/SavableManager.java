package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.config.ISavable;
import me.skitttyy.kami.api.feature.Feature;

import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavableManager implements IMinecraft
{

    public static final File MAIN_FOLDER = new File(System.getProperty("user.dir") + File.separator + "Sn0w");

    public static SavableManager INSTANCE;
    public boolean isLoading = false;


    public Yaml yaml;

    final List<ISavable> savables;

    public SavableManager()
    {
        savables = new ArrayList<>();
        if (!MAIN_FOLDER.exists())
        {
            MAIN_FOLDER.mkdir();
        }
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
        isLoading = false;
    }

    public List<ISavable> getSavables()
    {
        return savables;
    }

    public void load()
    {
        for (ISavable savable : getSavables())
        {
            try
            {
                File dir = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + savable.getDirName());
                if (!dir.exists()) dir.mkdirs();
                File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + savable.getDirName() + File.separator + savable.getFileName());
                if (!file.exists())
                {
                    file.createNewFile();
                } else
                {
                    InputStream inputStream = new FileInputStream(file);

                    Map<String, Object> map = yaml.load(inputStream);

                    savable.load(map);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void save() throws IOException
    {
        System.out.println("Saving your config");

        for (ISavable savable : getSavables())
        {
            File dir = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + savable.getDirName());
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + savable.getDirName() + File.separator + savable.getFileName());
            if (!file.exists())
            {
                file.createNewFile();
            }
            try
            {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                yaml.dump(savable.save(), new FileWriter(file));
            } catch (Throwable exception)
            {
                exception.printStackTrace();
            }
        }
        SearchManager.INSTANCE.save();
    }

    public void saveModuleConfig(String name) throws IOException
    {
        System.out.println("Saving your config");
        File dir = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs" + File.separator);

        if (!dir.exists()) dir.mkdirs();
        File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs" + File.separator + name + ".yml");

        if (!file.exists()) file.createNewFile();

        Map<String, Object> yamlData = new HashMap<String, Object>();
        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.putAll(feature.save());

            yamlData.put(feature.getName(), featureMap);
        }
        try
        {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            yaml.dump(yamlData, new FileWriter(file));
        } catch (Throwable exception)
        {
            exception.printStackTrace();
        }
    }

    public void loadModuleConfig(String name) throws Exception
    {

        System.out.println("Loading config");
        File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs" + File.separator + name + ".yml");
        if (!file.exists()) throw new Exception("Could not find config " + name + ".yml!");


        InputStream inputStream = new FileInputStream(file);

        Map<String, Object> map = yaml.load(inputStream);

        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            try
            {
                Map<String, Object> featureMap = (Map<String, Object>) map.get(feature.getName());
                feature.loadModule(featureMap);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void softLoadConfig(String name) throws Exception
    {
        System.out.println("Loading config");
        File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs" + File.separator + name + ".yml");
        if (!file.exists()) throw new Exception("Could not find config " + name + ".yml!");


        InputStream inputStream = new FileInputStream(file);

        Map<String, Object> map = yaml.load(inputStream);

        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            if (feature.getCategory().equals(Feature.Category.Render) || feature.getCategory().equals(Feature.Category.Hud))
                continue;


            if (feature.getCategory().equals(Feature.Category.Client) && feature != AntiCheat.INSTANCE)
                continue;

            try
            {
                Map<String, Object> featureMap = (Map<String, Object>) map.get(feature.getName());
                feature.softLoadModule(featureMap);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void deleteModuleConfig(String name) throws Exception
    {

        System.out.println("Loading config");
        File file = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs" + File.separator + name + ".yml");

        if (!file.exists()) throw new Exception("Could not find config " + name + ".yml!");

        file.delete();

    }

    public void listConfigs() throws Exception
    {

        File dir = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs");
        File[] directories = dir.listFiles(File::isFile);
        ChatUtils.sendMessage(Formatting.BLUE + "Available Configs:");

        for (File file : directories)
        {
            System.out.println("" + file.getName().toString());
            ChatUtils.sendMessage(file.getName());
        }

    }

    public void openFolder()
    {

        File dir = new File(MAIN_FOLDER.getAbsolutePath() + File.separator + "configs");
        if (!dir.exists()) dir.mkdir();

        Util.getOperatingSystem().open(dir);
    }

    public static File createFileIfNotExists(String name, String exstension) throws IOException
    {
        File dir = new File(mc.runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        File namedFile = new File(mc.runDirectory.getAbsolutePath(), File.separator + "Sn0w" + File.separator + name + "." + exstension);
        if (!namedFile.exists())
        {
            namedFile.createNewFile();
        }
        return namedFile;
    }

    public static void createFileIfNotExistsRaw(File file) throws IOException
    {
        if (!file.exists())
        {
            file.createNewFile();
        }
    }

    public static void appendToFile(BufferedWriter bufferedWriter, String text) throws IOException
    {
        if (bufferedWriter != null)
        {
            bufferedWriter.append(text + "\r\n");
        }
        assert bufferedWriter != null;
        bufferedWriter.close();
    }

    public static BufferedWriter makeWriter(File file, boolean append) throws IOException
    {
        return new BufferedWriter(new FileWriter(file, true));
    }

    public static void writeToFile(BufferedWriter bufferedWriter, String text) throws IOException
    {
        if (bufferedWriter != null)
        {
            bufferedWriter.write(text + "\r\n");
        }
    }

    public static void closeWriter(BufferedWriter bufferedWriter) throws IOException
    {
        bufferedWriter.close();
    }

    public static void clearFile(File file) throws IOException
    {
        PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
        printWriter.write("");
        printWriter.close();
    }

}
