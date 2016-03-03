package com.github.atomsponge.skyblockmp.grid;

import lombok.Data;

/**
 * @author AtomSponge
 */
@Data
public class Position {
    private final int x;
    private final int z;

    public boolean isCenterPosition() {
        return isCenterPosition(x, z);
    }

    public static boolean isCenterPosition(int x, int z) {
        return x == 0 && z == 0;
    }
}
