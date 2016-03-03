package com.github.atomsponge.skyblockmp.world.worldproviders;

import com.github.atomsponge.skyblockmp.world.chunkproviders.SkyblockSurfaceChunkProvider;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author AtomSponge
 */
public class SkyblockSurfaceWorldProvider extends WorldProviderSurface {
    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    public ChunkCoordinates getSpawnPoint() {
        return new ChunkCoordinates(0, 81, 0);
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        return new SkyblockSurfaceChunkProvider(worldObj);
    }
}
