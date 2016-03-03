package com.github.atomsponge.skyblockmp.world;

import com.github.atomsponge.skyblockmp.world.chunkproviders.SkyblockSurfaceChunkProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * @author AtomSponge
 */
public class SkyblockWorldType extends WorldType {
    public SkyblockWorldType() {
        super("SKYBLOCKMP");
    }

    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
        return new SkyblockSurfaceChunkProvider(world);
    }

    @Override
    public boolean isCustomizable() {
        return false;
    }

    @Override
    public int getSpawnFuzz() {
        // Don't scatter the player around the actual spawn location
        return 1;
    }
}
