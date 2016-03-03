package com.github.atomsponge.skyblockmp.world.chunkproviders;

import com.github.atomsponge.skyblockmp.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderHell;

/**
 * @author AtomSponge
 */
public class SkyblockHellChunkProvider extends ChunkProviderHell {
    private final World world;

    public SkyblockHellChunkProvider(World world, long seed) {
        super(world, seed);
        this.world = world;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return provideChunk(x, z);
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Block[] blocks = new Block[WorldUtils.CHUNK_DATA_SIZE];
        genNetherBridge.func_151539_a(this, world, x, z, blocks);
        Chunk chunk = new Chunk(world, blocks, x, z);
        WorldUtils.applyBiomes(chunk, world, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        genNetherBridge.generateStructuresInChunk(world, world.rand, x, z);
    }
}
