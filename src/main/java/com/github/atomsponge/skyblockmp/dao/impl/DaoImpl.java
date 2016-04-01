package com.github.atomsponge.skyblockmp.dao.impl;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.database.DatabaseManager;
import lombok.Getter;

/**
 * @author AtomSponge
 */
@Getter
public abstract class DaoImpl {
    private final SkyblockMp mod;
    private final DatabaseManager databaseManager;

    public DaoImpl(SkyblockMp mod) {
        this.mod = mod;
        this.databaseManager = mod.getDatabaseManager();
    }
}
