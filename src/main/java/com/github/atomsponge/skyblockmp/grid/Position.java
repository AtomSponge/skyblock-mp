package com.github.atomsponge.skyblockmp.grid;

import com.sk89q.worldedit.Vector;
import lombok.Data;

/**
 * @author AtomSponge
 */
@Data
public class Position {
    private final GridManager gridManager;
    private final int x;
    private final int z;

    public int getBlockX() {
        return gridManager.getSpacing() * x;
    }

    public int getBlockZ() {
        return gridManager.getSpacing() * z;
    }

    public boolean isCenterPosition() {
        return isCenterPosition(x, z);
    }

    public Vector toVector() {
        return new Vector(getBlockX(), gridManager.getIslandHeight(), getBlockZ());
    }

    public static boolean isCenterPosition(int x, int z) {
        return x == 0 && z == 0;
    }
}
