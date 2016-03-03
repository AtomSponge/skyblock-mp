package com.github.atomsponge.skyblockmp.world.worldproviders;

import com.github.atomsponge.skyblockmp.world.chunkproviders.SkyblockEndChunkProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author AtomSponge
 */
public class SkyblockEndWorldProvider extends WorldProviderEnd {
    @Override
    public IChunkProvider createChunkGenerator() {
        return new SkyblockEndChunkProvider(worldObj, getSeed());
    }
}
