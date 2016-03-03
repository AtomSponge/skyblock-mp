package com.github.atomsponge.skyblockmp.world.worldproviders;

import com.github.atomsponge.skyblockmp.world.chunkproviders.SkyblockHellChunkProvider;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author AtomSponge
 */
public class SkyblockHellWorldProvider extends WorldProviderHell {
    @Override
    public IChunkProvider createChunkGenerator() {
        return new SkyblockHellChunkProvider(worldObj, getSeed());
    }
}
