package com.github.atomsponge.skyblockmp.world.generators;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * @author AtomSponge
 */
public class SpawnPlatformGenerator extends PlatformGenerator {
    @Override
    protected void doGenerate(World world, int x, int y, int z) {
        for (int width = -3; width < 4; width++) {
            for (int depth = -3; depth < 4; depth++) {
                Block targetBlock = Math.abs(width) == 3 || Math.abs(depth) == 3 ? Blocks.quartz_block : Blocks.stone;
                world.setBlock(x + width, y - 1, z + depth, targetBlock);
            }
        }
    }
}
