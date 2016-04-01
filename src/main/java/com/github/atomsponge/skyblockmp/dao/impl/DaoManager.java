package com.github.atomsponge.skyblockmp.dao.impl;

import com.github.atomsponge.skyblockmp.SkyblockMp;
import com.github.atomsponge.skyblockmp.dao.Dao;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author AtomSponge
 */
@RequiredArgsConstructor
public class DaoManager {
    private final SkyblockMp mod;
    private final Map<Class<?>, DaoImpl> daoByModel = new HashMap<>();

    public void registerDao(Class<?> modelClass, DaoImpl dao) {
        daoByModel.put(modelClass, dao);
    }

    public Dao getDao(Class<?> modelClass) {
        return (Dao) daoByModel.get(modelClass);
    }
}
