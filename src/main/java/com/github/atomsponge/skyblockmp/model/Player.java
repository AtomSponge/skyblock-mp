package com.github.atomsponge.skyblockmp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * @author AtomSponge
 */
@Data
@AllArgsConstructor
public class Player {
    private int id;
    private UUID uuid;
    private String lastUsername;
    private int defaultIsland;
}
