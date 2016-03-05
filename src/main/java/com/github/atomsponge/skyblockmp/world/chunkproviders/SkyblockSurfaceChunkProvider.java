package com.github.atomsponge.skyblockmp.world.chunkproviders;

import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.util.WorldUtils;
import com.github.atomsponge.skyblockmp.world.generators.SpawnPlatformGenerator;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;

import java.util.List;

/**
 * @author AtomSponge
 */
public class SkyblockSurfaceChunkProvider extends ChunkProviderFlat {
    private static final byte BIOME_ID = (byte) BiomeGenBase.forest.biomeID;

    private final World world;

    public SkyblockSurfaceChunkProvider(World world) {
        super(world, world.getSeed(), false, null);
        this.world = world;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Block[] blocks = new Block[WorldUtils.CHUNK_DATA_SIZE];
        Chunk chunk = new Chunk(world, blocks, x, z);
        WorldUtils.applyBiomes(chunk, world, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return provideChunk(x, z);
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        if (Position.isCenterPosition(x, z)) {
            SpawnPlatformGenerator generator = new SpawnPlatformGenerator();
            generator.generate(world, x * 16, 80, z * 16);
        }
    }
}
