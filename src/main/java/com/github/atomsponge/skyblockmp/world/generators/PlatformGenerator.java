package com.github.atomsponge.skyblockmp.world.generators;

import net.minecraft.world.World;

/**
 * @author AtomSponge
 */
public abstract class PlatformGenerator {
    public void generate(World world, int x, int y, int z) {
        world.scheduledUpdatesAreImmediate = true;
        doGenerate(world, x, y, z);
        world.scheduledUpdatesAreImmediate = false;
    }

    protected abstract void doGenerate(World world, int x, int y, int z);
}
