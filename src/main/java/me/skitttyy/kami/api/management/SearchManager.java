package me.skitttyy.kami.api.management;

import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchManager {
    private final List<Block> SEARCH_BLOCKS;
    public static SearchManager INSTANCE;
    public static final File SEARCH_FILE = new File(System.getProperty("user.dir") + File.separator + "Sn0w" + File.separator + "misc" + File.separator + "search.yml");


    public SearchManager()
    {
        SEARCH_BLOCKS = new ArrayList<>();

        //add default blocks
        addBlocks();
        try
        {
            if (!SEARCH_FILE.exists())
            {
                SEARCH_FILE.createNewFile();
            }
            intializeBlocks();
        } catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    public void addBlocks()
    {
        SEARCH_BLOCKS.add(Blocks.NETHER_PORTAL);
        SEARCH_BLOCKS.add(Blocks.ENDER_CHEST);
        SEARCH_BLOCKS.add(Blocks.CHEST);
        SEARCH_BLOCKS.add(Blocks.DISPENSER);
        SEARCH_BLOCKS.add(Blocks.TRAPPED_CHEST);

        //SHULKER BLOCKS
        SEARCH_BLOCKS.addAll(BlockUtils.SHULKER_BLOCKS);
    }

    public boolean hasBlock(Block block)
    {
        return SEARCH_BLOCKS.contains(block);
    }

    public Block findBlock(String name)
    {
        return StringUtils.parseId(Registries.BLOCK, name);
    }

    public void intializeBlocks() throws IOException
    {
        InputStream friendStream = new FileInputStream(SEARCH_FILE);
        Map<String, Map<String, Object>> yamlObj = new Yaml().load(friendStream);
        if (yamlObj != null)
        {
            for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet())
            {
                addBlock(String.valueOf(pathEntry.getValue()));
            }
        }
    }


    public void addBlock(String name)
    {
        Block block;
        if ((block = StringUtils.parseId(Registries.BLOCK, name)) != null)
        {
            if (!SEARCH_BLOCKS.contains(block))
                SEARCH_BLOCKS.add(block);
        }
    }


    public void removeBlock(Block block)
    {
        SEARCH_BLOCKS.remove(block);
    }

    public void save() throws IOException
    {
        SavableManager.clearFile(SEARCH_FILE);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        for (Block block : SEARCH_BLOCKS)
        {
            String name = block.getName().getString();
            yamlData.put(name, Registries.BLOCK.getId(block).toString());
        }
        Yaml yaml = new Yaml();
        BufferedWriter bufferedWriter = SavableManager.makeWriter(SEARCH_FILE, false);
        yaml.dump(yamlData, bufferedWriter);
        SavableManager.closeWriter(bufferedWriter);
    }

}
