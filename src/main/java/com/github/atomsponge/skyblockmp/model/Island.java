package com.github.atomsponge.skyblockmp.model;

import com.github.atomsponge.skyblockmp.grid.Position;
import com.sk89q.worldedit.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author AtomSponge
 */
@Data
@AllArgsConstructor
public class Island {
    private int id;
    private Position position;
    private int owner;
    private Vector spawnOffset;

    public Vector getSpawnPosition() {
        return position.toVector().add(spawnOffset);
    }
}
