package com.github.atomsponge.skyblockmp.dao;

import com.github.atomsponge.skyblockmp.dao.impl.DaoException;
import com.github.atomsponge.skyblockmp.model.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author AtomSponge
 */
public interface PlayerDao extends Dao {
    List<Player> findByUuid(UUID uuid) throws DaoException;

    boolean insertPlayer(Player player) throws DaoException;

    boolean updatePlayer(Player player) throws DaoException;
}
