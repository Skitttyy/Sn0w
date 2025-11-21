package me.skitttyy.kami.impl.features.modules.render;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.event.events.world.ChunkDataEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.RenderType;
import me.skitttyy.kami.api.utils.world.WorldUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.*;

import java.util.ArrayList;
import java.util.List;

import static me.skitttyy.kami.impl.features.modules.render.NewChunks.BiomeCheckResult.*;
import static net.minecraft.world.World.*;

public class NewChunks extends Module
{

    private final IntSet presentStateIdsBuf = new IntOpenHashSet();


    public NewChunks()
    {
        super("NewChunks", Category.Render);
    }

    private List<Chunk> chunkList = new ArrayList<>();


    @Override
    public void onEnable()
    {
        super.onEnable();
        if (NullUtils.nullCheck()) return;


        chunkList.clear();
    }
    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;


        chunkList.clear();
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (NullUtils.nullCheck()) return;


        synchronized (chunkList) {
            for (Chunk chunk : chunkList) {
                ChunkPos pos = chunk.getPos();
                if (pos != null && mc.getCameraEntity().getBlockPos().isWithinDistance(pos.getStartPos(), (mc.options.getViewDistance().getValue() * 3) * 16)) {
                    Box box = new Box(new Vec3d(pos.getStartPos().getX(), pos.getStartPos().getY() + 0.1, pos.getStartPos().getZ()), new Vec3d(pos.getStartPos().getX() + 16, pos.getStartPos().getY() + 0.1, pos.getStartPos().getZ() + 16));
                    RenderUtil.renderBox(
                            RenderType.LINES,
                            box,
                            HudColors.getTextColor(0),
                            HudColors.getTextColor(0)
                    );
                }
            }
        }
    }


    @SubscribeEvent
    public void onChunkData(ChunkDataEvent event)
    {
        if (event.isSeenChunk()) return; // never will be newchunk if we've already cached it
        var dim = WorldUtils.getActualDimension();
        var chunk = event.getChunk();
        var x = chunk.getPos().x;
        var z = chunk.getPos().z;
        try
        {

            if (isNewChunk(dim, chunk)) chunkList.add(chunk);
        } catch (final Exception e)
        {
//            XaeroPlus.LOGGER.error("Error checking palette NewChunk at [{} {}]", x, z, e);
        }
    }


    private boolean isNewChunk(RegistryKey<World> dim, WorldChunk chunk)
    {
        if (dim == OVERWORLD)
        {
            return switch (checkNewChunkBiomePalette(chunk, true))
            {
                case NO_PLAINS -> false;
                case PLAINS_IN_PALETTE -> true;
                case PLAINS_PRESENT -> checkNewChunkBlockStatePalette(chunk);
            };
        } else if (dim == NETHER)
        {
            return checkNewChunkBiomePalette(chunk, false) == PLAINS_IN_PALETTE;
        } else if (dim == END)
        {
            return checkNewChunkBiomePalette(chunk, false) == PLAINS_IN_PALETTE;
        }
        return false;
    }

    /**
     * MC generates chunks in multiple steps where each step progressively mutates the chunk data
     * For more info see this explanation by Henrik Kniberg: https://youtu.be/ob3VwY4JyzE&t=453
     * <p>
     * When a chunk is first generated it is populated first by air, then by additional block types like stone, water, etc
     * By the end of these steps, the chunk's blockstate palette will still contain references to all states that were ever present
     * For more info on what chunk palettes are see: https://wiki.vg/Chunk_Format#Paletted_Container_structure
     * <p>
     * When the MC server writes + reads the chunks to region files it compacts the palette to save disk space
     * the key is that this compaction occurs _after_ newly generated chunk data is sent to players
     * <p>
     * compacting has 2 effects:
     * 1. palette entries without values present in the chunk are removed
     * 2. the order of ids in the palette can change as it is rebuilt in order of the actual blockstates present in the chunk
     * <p>
     * So we are simply checking if the first entry of the lowest section's block palette is air
     * The lowest section should always have bedrock as the first entry at the bottom section after compacting
     * Credits to etianl (https://github.com/etianl/Trouser-Streak) for first idea and public implementation for examining palette entries
     * and crosby (https://github.com/RacoonDog) for idea to check if air is the first palette entry
     * <p>
     * However, there is a chance for false negatives if the chunk's palette generates with more than 16 different blockstates
     * The palette gets resized to a HashMapPalette which does not retain the original entry ordering
     * Usually this happens when features like mineshafts or the deep dark generates
     * <p>
     * The second check that can be applied is verifying every palette entry is actually present in the data.
     * But this can still fail if air is still present in the section. Or if the chunk is modified by a
     * different online player right before we enter it.
     */
    private boolean checkNewChunkBlockStatePalette(WorldChunk chunk)
    {
        var sections = chunk.getSectionArray();
        if (sections.length == 0) return false;
        var firstSection = sections[0];
        Palette<BlockState> firstPalette = firstSection.getBlockStateContainer().data.palette();
        if (isNotLinearOrHashMapPalette(firstPalette)) return false;
        if (firstPalette instanceof ArrayPalette<BlockState>)
        {
            return firstPalette.get(0).isOf(Blocks.AIR);
        } else
        { // HashMapPalette
            // we could iterate through more sections but this is good enough in most cases
            // checking every blockstate is relatively expensive
            for (int i = 0; i < Math.min(sections.length, 5); i++)
            {
                var section = sections[i];
                var paletteContainerData = section.getBlockStateContainer().data;
                var palette = paletteContainerData.palette();
                if (isNotLinearOrHashMapPalette(palette)) continue;
                if (checkForExtraPaletteEntries(paletteContainerData)) return true;
            }
        }
        return false;
    }

    /**
     * Same logic as BlockState palette but we check the biomes palette.
     * MC initializes palettes with the Plains biome.
     * <p>
     * This check is very reliable in all dimensions - even the overworld as long as plains is not a real biome present.
     * <p>
     * This should generally be preferred over blockstate palette checks as its faster and more reliable.
     * For example, this solves the issue of player activity modifying the chunk, and therefore possibly causing palette ID's
     * without matching data present, at the same time as we load them.
     */
    private BiomeCheckResult checkNewChunkBiomePalette(WorldChunk chunk, boolean checkData)
    {
        var sections = chunk.getSectionArray();
        if (sections.length == 0) return NO_PLAINS;
        var firstSection = sections[0];
        var biomes = firstSection.getBiomeContainer();
        if (biomes instanceof PalettedContainer<RegistryEntry<Biome>> biomesPaletteContainer)
        {
            var palette = biomesPaletteContainer.data.palette();
            boolean paletteContainsPlains = palette.hasAny(NewChunks::isPlainsBiome);
            if (paletteContainsPlains && checkData)
            {
                if (palette.getSize() == 1) return PLAINS_PRESENT;
                var storage = biomesPaletteContainer.data.storage();
                presentStateIdsBuf.clear();
                storage.forEach(presentStateIdsBuf::add);
                for (int id : presentStateIdsBuf)
                {
                    if (isPlainsBiome(palette.get(id)))
                    {
                        return PLAINS_PRESENT;
                    }
                }
            }
            if (paletteContainsPlains) return PLAINS_IN_PALETTE;
        }
        return NO_PLAINS;
    }

    enum BiomeCheckResult
    {
        NO_PLAINS,
        PLAINS_IN_PALETTE,
        PLAINS_PRESENT
    }


    private boolean isNotLinearOrHashMapPalette(Palette palette)
    {
        return palette.getSize() <= 0 || !(palette instanceof ArrayPalette) && !(palette instanceof BiMapPalette);
    }

    private synchronized boolean checkForExtraPaletteEntries(PalettedContainer.Data<BlockState> paletteContainer)
    {
        presentStateIdsBuf.clear(); // reusing to reduce gc pressure
        var palette = paletteContainer.palette();
        PaletteStorage storage = paletteContainer.storage();
        storage.forEach(presentStateIdsBuf::add);
        return palette.getSize() > presentStateIdsBuf.size();
    }

    private static boolean isPlainsBiome(RegistryEntry<Biome> holder)
    {
        return holder.matchesKey(BiomeKeys.PLAINS);
    }


    @Override
    public String getDescription()
    {
        return "NewChunks: Uses an exploit to highlight newly generated chunks!";
    }
}
