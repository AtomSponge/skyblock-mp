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

import java.util.List;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor
public class SkyblockSurfaceChunkProvider implements IChunkProvider {
    private static final byte BIOME_ID = (byte) BiomeGenBase.forest.biomeID;

    private final World world;

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Block[] blocks = new Block[WorldUtils.CHUNK_DATA_SIZE];
        Chunk chunk = new Chunk(world, blocks, x, z);
        WorldUtils.applyBiomes(chunk, BIOME_ID);
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

    @Override
    public boolean saveChunks(boolean b, IProgressUpdate iProgressUpdate) {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return true;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public String makeString() {
        return "SkyblockEmptyLevelSource";
    }

    @Override
    public List getPossibleCreatures(EnumCreatureType enumCreatureType, int i, int i1, int i2) {
        return BiomeGenBase.forest.getSpawnableList(enumCreatureType);
    }

    @Override
    public ChunkPosition func_147416_a(World world, String s, int i, int i1, int i2) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int i, int i1) {
    }

    @Override
    public void saveExtraData() {
    }
}
