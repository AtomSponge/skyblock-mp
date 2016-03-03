package com.github.atomsponge.skyblockmp.world.chunkproviders;

import com.github.atomsponge.skyblockmp.grid.Position;
import com.github.atomsponge.skyblockmp.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.feature.WorldGenSpikes;

/**
 * @author AtomSponge
 */
public class SkyblockEndChunkProvider extends ChunkProviderEnd {
    private final World world;
    private WorldGenSpikes spikes = new WorldGenSpikes(Blocks.air);

    public SkyblockEndChunkProvider(World world, long seed) {
        super(world, seed);
        this.world = world;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return provideChunk(x, z);
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = new Chunk(world, new Block[WorldUtils.CHUNK_DATA_SIZE], x, z);
        WorldUtils.applyBiomes(chunk, world, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        // See https://github.com/LexManos/YUNoMakeGoodMap/blob/master/src/main/java/net/minecraftforge/lex/yunomakegoodmap/WorldProviderEndVoid.java
        if (x > -5 && x < 5 && z > -5 && z < 5 && world.rand.nextInt(5) == 0) {
            spikes.generate(world, world.rand,
                    x * 16 + world.rand.nextInt(16) + 8,
                    world.provider.getAverageGroundLevel(),
                    z * 16 + world.rand.nextInt(16) + 8);
        }

        if (Position.isCenterPosition(x, z)) {
            EntityDragon dragon = new EntityDragon(world);
            dragon.setLocationAndAngles(0.0D, 128.0D, 0.0D, world.rand.nextFloat() * 360.0F, 0.0F);
            world.spawnEntityInWorld(dragon);
        }
    }
}
