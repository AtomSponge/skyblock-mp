package com.github.atomsponge.skyblockmp.util;

import lombok.experimental.UtilityClass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/**
 * @author AtomSponge
 */
@UtilityClass
public class WorldUtils {
    public static final int CHUNK_DATA_SIZE = 32768;

    public static void applyBiomes(Chunk chunk, byte biomeId) {
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            biomeArray[i] = biomeId;
        }
    }

    public static void applyBiomes(Chunk chunk, World world, int x, int z) {
        byte[] biomeArray = chunk.getBiomeArray();
        BiomeGenBase[] generatedBiomes = world.getWorldChunkManager().loadBlockGeneratorData(null, x * 16, z * 16, 16, 16);
        for (int i = 0; i < biomeArray.length; i++) {
            biomeArray[i] = (byte) generatedBiomes[i].biomeID;
        }
    }
}
