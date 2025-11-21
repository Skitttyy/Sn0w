package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.wrapper.IMinecraft;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import static me.skitttyy.kami.api.wrapper.IMinecraft.mc;

public class WorldUtils implements IMinecraft
{
    public static int getDimensionId(RegistryKey<World> type)
    {
        if (type.equals(World.NETHER))
        {
            return -1;
        } else if (type.equals(World.END))
        {
            return 1;
        } else if (type.equals(World.OVERWORLD))
        {
            return 0;
        }
        return 0;
    }

    public static RegistryKey<World> getActualDimension() {
        try {
            return mc.world.getRegistryKey();
        } catch (Exception var1) {
            return World.OVERWORLD;
        }
    }

}
